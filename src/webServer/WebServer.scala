package webServer

import server._
import spray.routing.SimpleRoutingApp
import akka.actor.ActorSelection
import akka.actor.ActorSystem
import shapeless.get
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask
import akka.pattern.AskTimeoutException



sealed trait WebServerMsgs
case class GetFollowers(server: ActorSelection, userId: Int) extends WebServerMsgs

object WebServer extends App with SimpleRoutingApp {

  
  import system.dispatcher
  implicit val timeout = Timeout(2.seconds)

  val ipAddress = args(0)
  val port = Integer.parseInt(args(1))
  implicit val system = ActorSystem()
  val connectTo = "akka.tcp://TwitterServer@" + ipAddress + ":" + port + "/user/ServerSystem/workerRouter"
  val server = system.actorSelection(connectTo)
  
  
  startServer(interface = "192.168.0.23", port = 8080) {

    get {
      path("returnFollowers") {
        parameter("userId") { userId =>
          complete {
            (server ? ReturnFollowers(userId.toInt)).recover {
              case ex: AskTimeoutException => {
                "request failed"

              }
            }
              .mapTo[String]
              .map(s => s"The followers are : $s")
          }
        }

      }

    }~
    get {
      path("storedTweets") {
        parameter("userId") { userId =>
          complete {
            (server ? ReturnStoredTweets(userId.toInt)).recover {
              case ex: AskTimeoutException => {
                "request failed"

              }
            }
              .mapTo[String]
              .map(s => s"The stored tweets are : $s")
          }
        }

      }

    }~
    get {
      path("returnFollowings") {
        parameter("userId") { userId =>
          complete {
            (server ? ReturnFollowings(userId.toInt)).recover {
              case ex: AskTimeoutException => {
                "request failed"

              }
            }
              .mapTo[String]
              .map(s => s"your following is : $s")
          }
        }

      }

    }~
    post{
      path("tweet"){
        parameter("userId", "msg") { (userId, msg) =>
          complete {
             (server ? AddTweet(userId.toInt, msg)).recover {
              case ex: AskTimeoutException => {
                "tweet request failed"

              }
            }
              .mapTo[String]
             
            
            
          }
          
        }
        
      }
      
    }~
    post{
      path("retweet"){
        parameter("userId") { (userId) =>
          complete{
            (server ? Retweet(userId.toInt)).recover {
              case ex: AskTimeoutException => {
                "retweet request failed"

              }
            }
              .mapTo[String]
            
            
          }
          
        
        }
        
      }
      
    }

  }
}