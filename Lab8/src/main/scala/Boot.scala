import akka.actor.ActorSystem
import akka.pattern.ask
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import actor.CompanyManager
import model.{Company, ErrorResponse, Response, SuccessfulResponse}
import serializer.SprayJsonSerializer

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._

object Boot extends App with SprayJsonSerializer  {


  implicit val system = ActorSystem("company-service")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  implicit val timeout: Timeout = Timeout(10.seconds)

  val companyManager = system.actorOf(CompanyManager.props(), "company-manager")


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
            complete {
              (companyManager ? CompanyManager.ReadCompany(companyId)).mapTo[Either[ErrorResponse, Company]]
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
                complete {
                  (companyManager ? CompanyManager.CreateCompany(company)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
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
