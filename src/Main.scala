import akka.actor._
import server._
import client._
import scala.io.Source
import com.typesafe.config.ConfigFactory
import client.ConnectToServer
import client.Client
import server.Server

// starting server : server "noOfClients" "levelOfTwitter"
// starting client : client "ipAddressOfServer" "port" "levelOfTwitter"

object Main {

  def main(args: Array[String]) = {

    if (args(0) == "client") {

      val level = Integer.parseInt(args(5))
      val serverAddress = args(1)
      val serverPort = Integer.parseInt(args(2))
      val webServerAddress = args(3)
      val webServerPort = Integer.parseInt(args(4))
      
      System.setProperty("java.net.preferIPv4Stack", "true")
      val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=0").withFallback(ConfigFactory.load())
      val system = ActorSystem("TwitterClient", config)
      val client = system.actorOf(Props(new Client(serverAddress, serverPort,webServerAddress, webServerPort, level)), "ClientSystem")
      client ! ConnectToServer

    }

    if (args(0) == "server") {

      val noOfClients = Integer.parseInt(args(1))
      val level = Integer.parseInt(args(2))

      System.setProperty("java.net.preferIPv4Stack", "true")
      val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=2551").withFallback(ConfigFactory.load())
      val system = ActorSystem("TwitterServer", config)
      val server = system.actorOf(Props(new Server(noOfClients, level)), "ServerSystem")

    }

  }

}