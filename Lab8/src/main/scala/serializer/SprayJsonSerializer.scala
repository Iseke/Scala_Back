package serializer

import model.{Company, ErrorResponse, President, SuccessfulResponse}
import spray.json.DefaultJsonProtocol

trait SprayJsonSerializer extends DefaultJsonProtocol {

  implicit var presidentFormat = jsonFormat3(President)
  implicit var companyFormat = jsonFormat5(Company)

  implicit val successfulResponse = jsonFormat2(SuccessfulResponse)
  implicit val errorResponse = jsonFormat2(ErrorResponse)
}
