
import ConsumerActor.consume
import akka.actor.{Actor, ActorLogging}
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffsetBatch}
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.ExecutionContext.Implicits.global
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.Future

/**
  * Created by lokesh0973 on 2/18/2017.
  */
class ConsumerActor extends Actor with ActorLogging {
  implicit val mat = ActorMaterializer()
  val config = ConfigFactory.load()

  override def preStart(): Unit = {
    super.preStart()
    self ! consume
  }

  override def receive: Receive = {
    case ConsumerActor.consume => {
      val config = context.system.settings.config.getConfig("akka.kafka.consumer")
       val consumerSettings = ConsumerSettings(context.system, new ByteArrayDeserializer, new StringDeserializer)
          .withBootstrapServers("localhost:9092")
          .withGroupId("kafkatopicA")
          .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

        Consumer.committableSource(consumerSettings, Subscriptions.topics("kafkatopicA"))




      val producerSettings = ProducerSettings(context.system, new ByteArraySerializer, new StringSerializer)
        .withBootstrapServers("localhost:9092")

      val kafkaSink = Producer.plainSink(producerSettings)
      /*
      Synchronous put data to new kafka topic
      val (control, future) = Consumer.committableSource(consumerSetting, Subscriptions.topics("kafkaFile"))
        .map(processMessageSyn)
        .toMat(kafkaSink)(Keep.both)
        .run()*/




     //For kafkatopic 1 consumer with via option
     val source = Consumer.committableSource(consumerSettings,Subscriptions.topics("kafkatopicA"))
          .mapAsync(3)(processMessage)
          .via(Producer.flow(producerSettings))
          .map(_.message.passThrough)
          .groupedWithin(10, 15 seconds)
          .map(group=>group.foldLeft(CommittableOffsetBatch.empty){(batch,elem)=>batch.updated(elem)})
          .mapAsync(1)(_.commitScaladsl())

        val done = source.runWith(Sink.ignore)



    }
  }

  /*
  Synchronous put data to new kafka topic
  def processMessageSync(msg: ConsumerActor.Message): ProducerRecord[Array[Byte], String] = {
    println(msg.record.value())
    new ProducerRecord[Array[Byte], String]("kafkatopic1", msg.record.value().toLowerCase())
  }*/


  def processMessage(msg:ConsumerActor.Message) = {
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
      "kafkatopicB",
      "\""+newMsg
    ), msg.committableOffset))
  }
}
object ConsumerActor {
  type Message = CommittableMessage[Array[Byte],String]
  case object consume
}