package serializer

import com.sksamuel.elastic4s.{Hit, HitReader, Indexable}
import spray.json._
import model.Company

import scala.util.Try

trait ElasticSerializer extends SprayJsonSerializer {

  implicit object CompanyIndexable extends Indexable[Company] {
    override def json(company: Company): String = company.toJson.compactPrint
  }

  // JSON string -> object
  // parseJson is a Spray method
  implicit object CompanyHitReader extends HitReader[Company] {
    override def read(hit: Hit): Either[Throwable, Company] = {
      Try {
        val jsonAst = hit.sourceAsString.parseJson
        jsonAst.convertTo[Company]
      }.toEither
    }
  }
}
