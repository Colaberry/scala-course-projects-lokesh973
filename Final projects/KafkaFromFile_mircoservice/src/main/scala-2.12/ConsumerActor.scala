import ConsumerActor.Message
import akka.actor.{Actor, ActorLogging}
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffsetBatch}
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.Future

/**
  * Created by lokesh0973 on 2/18/2017.
  */
class ConsumerActor extends Actor with ActorLogging{
  implicit val mat = ActorMaterializer()
  override def receive:Receive = {
    case ConsumerActor.consume =>
      {
        val config = context.system.settings.config.getConfig("akka.kafka.consumer")
       /* val consumerSettings = ConsumerSettings(context.system, new ByteArrayDeserializer, new StringDeserializer)
          .withBootstrapServers("localhost:9092")
          .withGroupId("kafkaFile")
          .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

        Consumer.committableSource(consumerSettings, Subscriptions.topics("kafkaFile"))*/
        val consumerSetting = ConsumerSettings(context.system,new ByteArrayDeserializer, new StringDeserializer)
          .withBootstrapServers("localhost:9092")
          .withGroupId("kafkaFile")
          .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest")

        val (control,future) = Consumer.committableSource(consumerSetting,Subscriptions.topics("kafkaFile"))
          .mapAsync(1)(processMessage)
          .map(_.committableOffset)
          .groupedWithin(10, 15 seconds)
          .map(group=>group.foldLeft(CommittableOffsetBatch.empty){(batch,elem)=>batch.updated(elem)})
          .mapAsync(1)(_.commitScaladsl())
          .toMat(Sink.ignore)(Keep.both)
          .run()


    }
  }
  def processMessage(msg:Message): Future[Message] = {
    Future.successful(msg)
  }
}
object ConsumerActor {
  type Message = CommittableMessage[Array[Byte],String]
  case object consume
}