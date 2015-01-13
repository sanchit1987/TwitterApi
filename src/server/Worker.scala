package server

import akka.actor._
import server.Global
import server.TweetMsg

class Worker extends Actor {

  def receive = {

    case AddTweet(id: Int, message: String) => {

      val msg = new TweetMsg(id, message)
      Global.user(id).addMessage(msg)
      var followers = Global.user(id).returnFollowers
      followers.foreach(id => Global.user(id).addMessage(msg))
      Global.messagesProcessed += 1
      sender ! "tweet posted"

    }

    //*******************************************************************************************************************  

    case ReturnFollowers(userId: Int) => {
      //println("Server received request from: " + userId)
      sender ! Global.user(userId).returnFollowers()

    }

    //******************************************************************************************************************

    case Retweet(id: Int) => {

      val msg = Global.user(id).retweetingMsg

      if (msg != null) {

        val followers = Global.user(id).returnFollowers
        followers.foreach(i => Global.user(i).addMessage(msg))

        sender ! "retweet successful"

      } else sender ! "No message to retweet"
    }

    //******************************************************************************************************************

    case ReturnStoredTweets(id) => {

      
      sender ! Global.user(id).returnStoredTweets

    }

    //*******************************************************************************************************************

    case ReturnFollowings(id) => {

      sender ! Global.user(id).returnFollowings

    }

    //********************************************************************************************************************

    case MakeConnections(userId: Int, from: Int, to: Int) => {

      val rnd = new scala.util.Random
      val followersRange = from to to
      var noOfFollowers = followersRange(rnd.nextInt(followersRange length))
      var id = userId

     println(userId + "," + noOfFollowers)

      for (i <- 0 until noOfFollowers) {
        if (id == Global.totalUsers - 1) id = -1
        id += 1
        if (userId == id) println("yes")
        
        Global.user(userId).addFollower(id)
        Global.user(id).addFollowing(userId)

      }

      sender ! WorkCompleted
    }

    //*******************************************************************************************************************

  }

}