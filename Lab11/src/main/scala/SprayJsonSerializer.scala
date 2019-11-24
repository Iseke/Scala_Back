import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import models.{ErrorResponse, PathMd, SuccessfulResponse}


trait SprayJsonSerializer extends DefaultJsonProtocol {
  implicit val successfulFormat: RootJsonFormat[SuccessfulResponse] = jsonFormat2(SuccessfulResponse)
  implicit val errorFormat: RootJsonFormat[ErrorResponse] = jsonFormat2(ErrorResponse)

  implicit val pathFormat: RootJsonFormat[PathMd] = jsonFormat1(PathMd)
}
