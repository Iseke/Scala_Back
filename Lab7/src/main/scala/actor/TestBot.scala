package actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import model.{Director, ErrorResponse, Movie, SuccessfulResponse}

object TestBot {

  case object TestCreate

  case object TestConflict

  case object TestRead

  case object TestNotFound

  case object TestDelete

  case object TestUpdate

  def props(manager: ActorRef) = Props(new TestBot(manager))
}

class TestBot(manager: ActorRef)extends Actor with ActorLogging {

  import TestBot._

  override def receive: Receive = {
    case TestCreate=>
      manager ! MovieManager.CreateMovie(Movie("1","FastFurious", Director("dir-1","Ted",None,"Cooper"),2019))

    case TestConflict=>
      manager ! MovieManager.CreateMovie(Movie("222","Avengers:Infinite War",Director("dir-2","Joe",None,"Russo"),2018))
      manager ! MovieManager.CreateMovie(Movie("222","Spider-Man",Director("dir-3","Anthony",None,"Russo"),2019))

    case TestRead=>
      manager ! MovieManager.ReadMovie("1")

    case TestNotFound=>
      manager! MovieManager.ReadMovie("777")

    case TestUpdate=>
      manager ! MovieManager.UpdateMovie(Movie("1","Avengers:Final",Director("dir-2","Joe",None,"Russo"),2019))

    case TestDelete=>
      manager ! MovieManager.DeleteMovie("1")

    case SuccessfulResponse(status, msg)=>
      log.info("Received Successful Response with status: {} and message: {}",status,msg)

    case ErrorResponse(status, msg)=>
      log.warning("Received Error Response with status: {} and message: {} ",status,msg)

    case movie: Movie=>
      log.info("Received movie: [{}] ", movie)

  }

}
