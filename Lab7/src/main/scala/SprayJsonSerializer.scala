import spray.json.DefaultJsonProtocol
import model.{Director, ErrorResponse, Movie, SuccessfulResponse}


trait SprayJsonSerializer extends  DefaultJsonProtocol{
  //custom formats
  implicit val directorFormat = jsonFormat4(Director)
  implicit val movieFormat = jsonFormat4(Movie)

  implicit val successfulResponse = jsonFormat2(SuccessfulResponse)
  implicit val errorResponse = jsonFormat2(ErrorResponse)
}