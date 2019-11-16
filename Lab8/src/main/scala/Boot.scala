import akka.actor.ActorSystem
import akka.pattern.ask
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.{ActorMaterializer, Materializer}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.util.Timeout
import actor.CompanyManager
import model.{Company, ErrorResponse, Response, SuccessfulResponse}
import telegram_bot.{TelegramMessage, tgSerializer}

import org.slf4j.LoggerFactory
import serializer.SprayJsonSerializer

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Boot extends App with SprayJsonSerializer with tgSerializer {


  implicit val system = ActorSystem("company-service")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  implicit val timeout: Timeout = Timeout(10.seconds)

  val companyManager = system.actorOf(CompanyManager.props(), "company-manager")

  val token = "819883079:AAGtdezagpvgzx8ZAeltaHVTxpV0TAU_v88"
  val log = LoggerFactory.getLogger("Boot")

  log.info(s"Token: $token")


  def informBot(info:String):Unit = {
    val message: TelegramMessage = TelegramMessage(-371266564, info)

    val httpReq = Marshal(message).to[RequestEntity].flatMap { entity =>
      val request = HttpRequest(HttpMethods.POST, s"https://api.telegram.org/bot$token/sendMessage", Nil, entity)
      log.debug("Request: {}", request)
      Http().singleRequest(request)
    }

    httpReq.onComplete {
      case Success(value) =>
        log.info(s"Response: $value")
        value.discardEntityBytes()

      case Failure(exception) =>
        log.error(exception.getMessage)
    }

  }


  val route =
    path("check" / ) {
      get {
        complete {
          "OK"
        }
      }
    } ~
      pathPrefix("iseke-company") {
        path("company" / Segment) { companyId =>
          get {
            val dataFrom = (companyManager ? CompanyManager.ReadCompany(companyId)).mapTo[Either[ErrorResponse, Company]]

            onSuccess(dataFrom){
              case Right(value) => {
                informBot(s"Status: ok \n${value}")
                complete(200, value)
              }
              case Left(value) => {
                informBot(s"Status : error \n${value.message}")
                complete(value.status,value)
              }
            }
          } ~
            delete {
              complete {
                (companyManager ? CompanyManager.DeleteCompany(companyId)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
              }
            }
        } ~
          path("company") {
            post {
              entity(as[Company]) { company =>

                val dataFrom = (companyManager ? CompanyManager.CreateCompany(company)).mapTo[Either[ErrorResponse, SuccessfulResponse]]

                onSuccess(dataFrom){
                  case Right(company) => {
                    informBot(s"Status: ok \n${company.message}")
                    complete(200, company)
                  }
                  case Left(value) => {
                    informBot(s"Status : error \n${value.message}")
                    complete(value.status,value)
                  }
                }
              }
            } ~
              put {
                entity(as[Company]) { company =>
                  complete {
                    (companyManager ? CompanyManager.UpdateCompany(company)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
                  }
                }
              }
          }
      }

  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)


}
