package client


import akka.actor._
import spray.client.pipelining._
import java.io.Serializable
import spray.can.Http
import server._
import akka.actor.ActorSelection.toScala
import server.CreateUsers
import client.UserOnClient

sealed trait ClientMessage
case object ConnectToServer extends ClientMessage
case class CreateUserReferences(first: Int, last: Int) extends ClientMessage
case class StartTweeting(server: ActorRef) extends ClientMessage
case class Tweet(id: Int, message: String) extends ClientMessage
object StartMessaging extends ClientMessage with Serializable
object Shutdown extends ClientMessage with Serializable

//***************************************************************************************************************

//********************************************************************************************************************

class Client(serverAddress: String, serverPort: Int, webServerAddress: String, webServerPort: Int, level: Int) extends Actor {

  /// implicit val system = context.system

  val noOfUsers: Int = (1000000 * level) / 100
  Global.totalUsers = noOfUsers
  val connectTo = "akka.tcp://TwitterServer@" + serverAddress + ":" + serverPort + "/user/ServerSystem"
  val userRef = new Array[ActorRef](noOfUsers)
  val server = context.actorSelection(connectTo)

  def receive = {

    //**************************************************************************************************************************

    case ConnectToServer => {

      println("Connecting to server........")
      println("no of users created = " + noOfUsers)
      server ! CreateUsers(noOfUsers)

    }

    //**************************************************************************************************************************    

    case CreateUserReferences(first, last) => {

      println("creating user references on client")
      var j = -1
      for (i <- first to last) {
        j += 1
        userRef(j) = context.actorOf(Props(new UserOnClient(i, webServerAddress, webServerPort)), name = "user" + i)

      }
    }

    //*********************************************************************************************************************

    case StartMessaging => {

      println("starting to tweet ")
      for (i <- 0 until noOfUsers) {
        userRef(i) ! StartTweeting(sender)
      }

    }
    //*******************************************************************************************************************

    case Http.CommandFailed => {

      println("command failed")

    }

    //************************************************************************************************************************
    case Shutdown => {
      println("Client shutting down..............")
      context.system.shutdown

    }

  }

}