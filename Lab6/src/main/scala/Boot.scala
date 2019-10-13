import akka.actor.ActorSystem

import actor.{MovieManager, TestBot}
import model.{Director, Movie, Response}

object Boot extends  App{

  val system = ActorSystem("movie-service")

  val movieManager = system.actorOf(MovieManager.props(), "movie-manager")

  val testBot = system.actorOf(TestBot.props(movieManager), "test-bot")

  //Create
  testBot ! TestBot.TestCreate

  //Read
  testBot ! TestBot.TestRead

//Conflict
//  testBot ! TestBot.TestConflict

//NotFOund
//  testBot ! TestBot.TestNotFound

//Update
  testBot ! TestBot.TestUpdate

  //Read
  testBot ! TestBot.TestRead

//Delete
//  testBot ! TestBot.TestDelete


}
