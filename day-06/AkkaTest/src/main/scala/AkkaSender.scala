import akka.actor.{Actor, Props}
import akka.actor.Actor.Receive

/**
  * Created by lokesh0973 on 2/15/2017.
  */
class AkkaSender extends Actor{
  var count=1
  override def preStart() = {
    val receiver = context.actorOf(Props[AkkaReceiver], "SendReceive")
    receiver ! "start"

  }
  override def receive = {
    case "relay" => {
      count+=1
      if(count<100)
        {
          println("Hello")
          sender() ! "start"
        }
        else{
          sender() ! "stop"
      }
  }
    case end => {
      println("The End")
      context.stop(self)
    }
  }
}
