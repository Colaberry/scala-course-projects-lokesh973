import ConsumerActor.Message
import akka.actor.{Actor, ActorLogging, Props}
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffsetBatch}
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}

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

       /* val consumerSettings = ConsumerSettings(context.system, new ByteArrayDeserializer, new StringDeserializer)
          .withBootstrapServers("localhost:9092")
          .withGroupId("kafkaFile")
          .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

        Consumer.committableSource(consumerSettings, Subscriptions.topics("kafkaFile"))*/
        val consumerSetting = ConsumerSettings(context.system,new ByteArrayDeserializer, new StringDeserializer)
          .withBootstrapServers("localhost:9092")
          .withGroupId("kafkaFile")
          .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest")

     /*   val (control,future) = Consumer.committableSource(consumerSetting,Subscriptions.topics("kafkaFile"))
          .mapAsync(1)(processMessage)
          .map(_.committableOffset)
          .groupedWithin(10, 15 seconds)
          .map(group=>group.foldLeft(CommittableOffsetBatch.empty){(batch,elem)=>batch.updated(elem)})
          .mapAsync(1)(_.commitScaladsl())
          .toMat(Sink.ignore)(Keep.both)
          .run()*/

        val producerSettings = ProducerSettings(context.system, new ByteArraySerializer, new StringSerializer)
          .withBootstrapServers("localhost:9092")

        val source = Consumer.committableSource(consumerSetting,Subscriptions.topics("kafkaFile"))
          .mapAsync(3)(processMessage)
          .via(Producer.flow(producerSettings))
          //        .map(_.committableOffset)
          .map(_.message.passThrough)
          .groupedWithin(10, 15 seconds)
          .map(group=>group.foldLeft(CommittableOffsetBatch.empty){(batch,elem)=>batch.updated(elem)})
          .mapAsync(1)(_.commitScaladsl())

        val done = source.runWith(Sink.ignore)

        val elasticSearchActor = context.actorOf(Props[ElasticSearchActor],"elasticSearchActor")
        elasticSearchActor ! ElasticSearchActor.start
    }
  }
 /* def processMessage(msg:Message): Future[Message] = {
    println(msg.record.value())
    Future.successful(msg)
  }*/

  def processMessage(msg:Message) = {
    //  println(msg.record.value())
    val msgVal1 = msg.record.value()
    val msgVal = msgVal1.substring(1,msgVal1.length)
    val msgSplit:Array[String] = msgVal.split("\",\"")

  if(msgSplit(5).length() == 0)
    {
      msgSplit(5) = "unknown"
    }
    msgSplit(5) = msgSplit(5).toLowerCase()
    val newMsg = msgSplit.mkString("\",\"")

   Future.successful(ProducerMessage.Message(new ProducerRecord[Array[Byte], String](
      "kafkatopic2",
      "\""+newMsg
    ), msg.committableOffset))
  }
}
object ConsumerActor {
  type Message = CommittableMessage[Array[Byte],String]
  case object consume
}