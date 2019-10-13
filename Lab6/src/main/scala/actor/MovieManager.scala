package actor

import akka.actor.{Actor, ActorLogging, Props}
import model.{ErrorResponse, SuccessfulResponse, Movie}


//props
//messages
object MovieManager{
  //create
  case class CreateMovie(movie: Movie)
  //read
  case class ReadMovie(id: String)
  //update
  case class UpdateMovie(movie: Movie)
  //delete
  case class DeleteMovie(id:String)

  def props() = Props(new MovieManager)
}
// know about existing movies
// can create a movie
// can manage movie
class MovieManager extends Actor with ActorLogging {

  import MovieManager._


  var movies: Map[String, Movie] = Map()

  override def receive : Receive = {
    case CreateMovie(movie)=>
      movies.get(movie.id)match {
        case Some(existingMovie)=>
//          log.warning(s"Could not create a movie with ID: ${movie.id} because it already exists.")
          sender() ! ErrorResponse(409, s"Movie with ID: ${movie.id} already exists.")

        case None =>
          movies = movies + (movie.id -> movie)
//          log.info("Movie with ID: {} created.", movie.id)
          sender() ! SuccessfulResponse(201, s"Movie with ID: ${movie.id} created.")
      }

    case msg: ReadMovie =>
      movies.get(msg.id) match {
        case Some(existingMovie) =>
//          log.info("Movie with ID: {} found [READ].", existingMovie.id)
          sender() ! existingMovie

        case None =>
//          log.info("Movie with ID: {} not found [READ].", msg.id);
          sender() ! ErrorResponse(404, s"Movie with ID: ${msg.id} not found [READ].")
      }

    case UpdateMovie(movie)=>
      movies.get(movie.id) match {
        case Some(existingMovie)=>
          movies = movies + (movie.id -> movie)
          sender() ! SuccessfulResponse(201, s"Movie with ID: ${movie.id} updated.")

        case None =>
          sender() ! ErrorResponse(409, s"Movie with ID: ${movie.id} does not exists.")

      }

    case msg: DeleteMovie=>
      movies.get(msg.id) match {
        case Some(existingMovie)=>
          movies -= existingMovie.id
          sender() ! SuccessfulResponse(202, s"Movie with ID: ${existingMovie.id} deleted.")

        case None =>
         sender() ! ErrorResponse(444, s"Movie with ID: ${msg.id} does not exists.")
      }
  }

}
