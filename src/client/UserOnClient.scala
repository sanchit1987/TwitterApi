package client

import akka.actor._
import spray.client.pipelining._
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import org.apache.commons.httpclient.util.URIUtil
import common._
import common.Messages

class UserOnClient(id: Int, address: String, port: Int) extends Actor {
  val rnd = new scala.util.Random
  import context.dispatcher
  val userId = id
  val webServerAddress = address
  val webServerPort = port
  val webServer = "http://" + webServerAddress + ":" + webServerPort
  val returnFollowersApi = webServer + "/returnFollowers?userId="
  val retweetApi = webServer + "/retweet?userId="
  val tweetApi = webServer + "/tweet?userId="
  val storedTweetsApi = webServer + "/storedTweets?userId="
  val returnFollowingsApi = webServer + "/returnFollowings?userId="
  val api = Array(storedTweetsApi, returnFollowingsApi, returnFollowersApi, retweetApi, tweetApi)

  val pipeline = sendReceive

  def receive = {

    case StartTweeting(server) => {

      context.system.scheduler.schedule(Duration.create(600, TimeUnit.MILLISECONDS),
        Duration.create(1, TimeUnit.SECONDS)) {

          val apiRange = 0 to api.length - 1
          var i = apiRange(rnd.nextInt(apiRange length))

          if (i == 4) {
            val range = 0 to Messages.messages.length - 1
            var no = range(rnd.nextInt(range length))
            val result = pipeline(Post(URIUtil.encodeQuery(api(i) + userId + "&msg=" + Messages.messages(no))))
            /* result.foreach { response =>
              println(s"tweet request completed ${response.status} and content:\n${response.entity.asString}")

            }*/
          } else if (i <= 2) {

            val result = pipeline(Get(api(i) + userId))
            
           /* result.foreach { response =>
              println(s"Request completed with status ${response.status} and content:\n${response.entity.asString}")
            }*/
          } else if (i > 2) {

            val result = pipeline(Post(api(i) + userId))
            /*    result.foreach { response =>
              println(s"Request completed with status ${response.status} and content:\n${response.entity.asString}")
            }*/

          }

        }

    }

    //*******************************************************************************************************************

  }

}