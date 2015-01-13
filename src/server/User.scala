package server

import scala.collection.mutable
import Array._
import org.json4s.ShortTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization._
import server.TweetMsg
import scala.collection.mutable
import Array._
import scala.collection.mutable.SynchronizedBuffer

class User(id: Int) {
  private implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[String])))
  private var userId: Int = id
  private var followers = new mutable.ArrayBuffer[Integer]() with SynchronizedBuffer[Integer]
  private var followings = new mutable.ArrayBuffer[Integer]() with SynchronizedBuffer[Integer]
  private var tweet = new Array[TweetMsg](100)
  private var count = 0

  def addFollower(userId: Int) = {

    followers += userId

  }

  //*******************************************************************************************************************
  def addFollowing(userId: Int) = {

    followings += userId

  }

  //*******************************************************************************************************************

  def addMessage(message: TweetMsg) = {

    if (count < 100) {
      tweet(count) = message
      count += 1
    } else {

      count = 0
      tweet(count) = message

    }

  }
  //*******************************************************************************************************************

  def returnFollowers(): String = {

    var list: List[Int] = List()

    if (followers.length >= 10)
      for (i <- 0 until 10) {
        if (followers(i) != null)
          list = followers(i) :: list
      }

    return writePretty(followers)

  }

  //*******************************************************************************************************************

  def retweetingMsg: TweetMsg = {

    for (i <- 0 until count) {

      if (tweet(i).userId != userId) {
        tweet(i).addRetweeterId(userId)
        return tweet(i)
      }

    }

    return null
  }

  //*******************************************************************************************************************

  def returnFollowings(): String = {

    var list: List[Int] = List()

    if (followings.length >= 10)
      for (i <- 0 until 10) {

        list = followings(i) :: list
      }

    return writePretty(followings)

  }

  //*******************************************************************************************************************

  def returnStoredTweets(): String = {

    var list: List[String] = List()

    for (i <- 0 until 10) {

      if (tweet(i) != null)
        list = tweet(i).msg :: list

    }

    return writePretty(list)

  }

  //******************************************************************************************************************* 

}