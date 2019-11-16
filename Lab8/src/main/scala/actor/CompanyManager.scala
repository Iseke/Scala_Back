package actor

import model.{Company, ErrorResponse, President, SuccessfulResponse}
import akka.actor.{Actor, ActorLogging, Props}

import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.index.CreateIndexResponse
import com.sksamuel.elastic4s.http.{HttpClient, RequestFailure, RequestSuccess}
import serializer.ElasticSerializer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object CompanyManager {

  case class CreateCompany(company: Company)

  case class ReadCompany(id: String)

  case class UpdateCompany(company: Company)

  case class DeleteCompany(id:String)

  def props() = Props(new CompanyManager)

}

class CompanyManager extends Actor with ActorLogging with ElasticSerializer{

  import CompanyManager._

  val client = HttpClient(ElasticsearchClientUri("localhost", 9200))


  def createEsIndex() = {
    val cmd: Future[Either[RequestFailure, RequestSuccess[CreateIndexResponse]]] =
      client.execute { createIndex("movies") }


    cmd.onComplete {
      case Success(value) =>
        value.foreach {requestSuccess =>
          println(requestSuccess)}

      case Failure(exception) =>
        println(exception.getMessage)
    }
  }

  override def receive: Receive = {
    case CreateCompany(company) =>
      val replyTo = sender()
      val cmd = client.execute(indexInto("company" / "_doc").id(s"${company.id}").doc(company))

      cmd.onComplete {
        case Success(value) =>
          log.info("Company with ID: {} created.", company.id)
          replyTo ! Right(SuccessfulResponse(201, s"Comapny with ID: ${company.id} created."))

        case Failure(fail) =>
          log.warning(s"Could not create a company with ID: ${company.id} , it's already exists.")
          replyTo ! Left(ErrorResponse(409, s"Company with ID: ${company.id} already exists."))
      }

    case msg: ReadCompany =>
      val replyTo = sender()
      val cmd = client.execute {
        get(msg.id).from("company" / "_doc")
      }

      cmd.onComplete {
        case Success(either) =>
          either.map(e => e.result.safeTo[Company]).foreach { company => {
            company match {
              case Left(value) =>
                log.info("Company with ID: {} not found [READ].", msg.id);
                replyTo ! Left(ErrorResponse(404, s"Company with ID: ${msg.id} not found [READ]."))
              case Right(company) =>
                log.info("Company with ID: {} found [READ].", msg.id)
                replyTo ! Right(company)
            }
          }
          }
        case Failure(fail) =>
          println(fail.getMessage)
          log.error(s"Could not find a company with ID: ${msg.id} ")
      }

    case UpdateCompany(company) =>
      val replyTo = sender()
      val cmd = client.execute {
        update(company.id).in("company" / "_doc").doc(company)
      }

      cmd.onComplete {
        case Success(either) =>
          log.info("Company with ID: {} updated.", company.id)
          replyTo ! Right(SuccessfulResponse(200, s"Company with ID: ${company.id} updated."))

      }

    case msg: DeleteCompany =>
      val replyTo = sender()
      val cmd = client.execute {
        delete(msg.id).from("company" / "_doc")
      }

      cmd.onComplete {
        case Success(either) =>
          either.map(e => e.result.result.toString).foreach { company => {
            company match {
              case "deleted" =>
                log.info("Company with ID: {} deleted.", msg.id);
                replyTo ! Right(SuccessfulResponse(200, s"Company with ID: ${msg.id} deleted."))
              case "not_found" =>
                log.info("Company with ID: {} not found [DELETE].", msg.id);
                replyTo ! Left(ErrorResponse(404, s"Company with ID: ${msg.id} not found [DELETE]."))
            }
          }
          }

        case Failure(fail) =>
          println(fail.getMessage)
          log.error(s"Could not find a company with ID: ${msg.id} to Delete.")
      }
  }
}
