package server

import scala.collection.mutable
import server.User

object Global {
  var user = new mutable.HashMap[Integer, User]()
  var messagesProcessed = 0
  var totalUsers = 0

}