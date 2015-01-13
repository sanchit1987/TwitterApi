package server

import akka.actor._
import collection.mutable.ListBuffer
import akka.routing.RoundRobinRouter
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import client._
import common._
import java.io.FileWriter
import server.Global
import server.TweetMsg
import server.User
import server.Worker
import common.Messages

sealed trait ServerMessage
case class CreateUsers(noOfUsers: Int) extends ServerMessage
object StartCreatingConnections extends ServerMessage
case class AddTweet(userId: Int, message: String) extends ServerMessage
object ConnectionsDone extends ServerMessage
case class ReturnFollowers(userId: Int) extends ServerMessage
case class Retweet(userId: Int) extends ServerMessage
case class ReturnFollowings(userId: Int) extends ServerMessage
case class ReturnStoredTweets(userId: Int) extends ServerMessage
case class MakeConnections(id: Int, from: Int, to: Int) extends ServerMessage
object WorkCompleted extends ServerMessage

//************************************************************************************************************************

class Server(noOfClients: Int, level: Int) extends Actor {

  val rnd = new scala.util.Random
  import context.dispatcher
  val totalUsers = (1000000 * noOfClients * level) / 100
  Global.totalUsers = totalUsers
  var percentageTrend = Array(10, 10, 10, 10, 10, 10, 10, 10, 10, 5, 1, 1, 1, 1, 1)
  var followersTrend = Array(3, 9, 19, 36, 61, 98, 154, 246, 458, 819, 978, 1211, 1675, 2991, 24964)
  var initial = 0
  var id = -1
  val noOfWorkers = ((Runtime.getRuntime().availableProcessors()) * 1.5).toInt
  val workerRouter = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(noOfWorkers)), name = "workerRouter")
  val time = 60
  var listOfClients = new ListBuffer[ActorRef]
  var count = 0
  var temp = 0

  //**********************************************************************************************************************

  /*  def makeConnections(userId: Int, noFrom: Int, noTo: Int) {

    val rnd = new scala.util.Random
    val followersRange = noFrom to noTo
    var noOfFollowers = followersRange(rnd.nextInt(followersRange length))

    println(userId + "," + noOfFollowers)

    for (i <- 0 until noOfFollowers) {

      val userIdRange = (0 to totalUsers - 1).filter(x => x != userId)
      var followerId = userIdRange(rnd.nextInt(userIdRange length))

      Global.user(userId).addFollower(followerId)
      Global.user(followerId).addFollowing(userId)

    }

  }*/

  //*****************************************************************************************************************  

  def receive = {

    case CreateUsers(noOfUsers) => {

      
      
      listOfClients += sender
      sender ! CreateUserReferences(initial, initial + noOfUsers - 1)
      initial = initial + noOfUsers
      count += 1

      if (count == noOfClients)
        self ! StartCreatingConnections

    }

    //****************************************************************************************************************  
    case StartCreatingConnections => {

      println("simulating connection")
      var from = 0
      var to = 0

      for (i <- 0 until totalUsers) {

        Global.user(i) = new User(i)

      }

      for (i <- 0 until percentageTrend.length) {
        var times = (totalUsers * percentageTrend(i)) / 100
        if (i == 0) {
          from = 1
          to = followersTrend(i)
        } else {
          from = followersTrend(i - 1)
          to = followersTrend(i)
        }
        for (j <- 0 until times) {
          id += 1
          workerRouter ! MakeConnections(id, from, to)
        }
      }

    }

    //********************************************************************************************************************   

    case ConnectionsDone => {

      for (i <- 0 until totalUsers) {

        val range = 0 to Messages.messages.length - 1
        var no = range(rnd.nextInt(range length))
        val msg = new TweetMsg(i, Messages.messages(no))
        Global.user(i).addMessage(msg)
        var followers = Global.user(i).returnFollowers
        followers.foreach(x => Global.user(x).addMessage(msg))

      }

      for (client <- listOfClients) {
        client ! StartMessaging

      }

      var p = System.getProperty("user.dir") + "/Main.txt"
      val pw = new java.io.PrintWriter(new FileWriter(p, false))



      context.system.scheduler.schedule(Duration.create(3, TimeUnit.SECONDS),
        Duration.create(1, TimeUnit.SECONDS)) {

          pw.write(Global.messagesProcessed + " ")
          println("message processed per second =" + Global.messagesProcessed)
          Global.messagesProcessed = 0

        }

      context.system.scheduler.scheduleOnce(time.seconds) {
        pw.close()
        println("shutting down the system")
        context.system.shutdown
        for (client <- listOfClients)
          client ! Shutdown

      }

    }

    //******************************************************************************************************************

    case WorkCompleted => {

      temp += 1
      if (temp == totalUsers) {

        println("ALL CONNECTIONS CREATED")
        self ! ConnectionsDone

      }

    }

    //******************************************************************************************************************    

  } // receive close 
}// class close