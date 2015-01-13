package server

import scala.collection.mutable

class TweetMsg(id : Int,tweet : String) {

  var msg: String = tweet
  var userId = id
  var retweetedBy = new mutable.HashSet[Integer]()
  
  def addRetweeterId(id : Int){
    
    retweetedBy.add(id)
    
  }

}