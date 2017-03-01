import akka.actor.Actor
import akka.actor.Actor.Receive

/**
  * Created by lokesh0973 on 2/15/2017.
  */
class AkkaReceiver extends Actor {
  var count = 0
  override def receive = {
    case "start" => {
      println("Hi")
      sender() ! "relay"
    }
    case "stop" => {
      sender ! "stop"
    }

  }
}
