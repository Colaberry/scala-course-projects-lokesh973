
import ConsumerActor.consume
import akka.actor.{Actor, ActorLogging}
import akka.event.slf4j.Logger
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
  * ConsumerActor has the core logic of this micro
  */
class ConsumerActor extends Actor with ActorLogging {
  implicit val mat = ActorMaterializer()
  val config = ConfigFactory.load()

  override def preStart(): Unit = {
    /**
      * Prestart will be call once the actor object is created. We can have any initializations in preStart
      */

    super.preStart()
    //Calls receive method and matches case with object consume
    self ! consume
  }

  override def receive: Receive = {
    case ConsumerActor.consume => {
      Logger("Initializing source settings for kafka consumer")
      //Initializing consumer settings
       val consumerSettings = ConsumerSettings(context.system, new ByteArrayDeserializer, new StringDeserializer)
          .withBootstrapServers(config.getString("kafka.broker-list"))
          .withGroupId(config.getString("kafka.topic1"))
          .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

        Consumer.committableSource(consumerSettings, Subscriptions.topics(config.getString("kafka.topic1")))


      Logger("Initializing sink settings for kafka producer")
      //Initializing settings for producer that takes records on new topic
      val producerSettings = ProducerSettings(context.system, new ByteArraySerializer, new StringSerializer)
        .withBootstrapServers(config.getString("kafka.broker-list"))

      Logger("Materializing stream")
      //Initializing sink for producer
      val kafkaSink = Producer.plainSink(producerSettings)
      /*
      Synchronous put data to new kafka topic
      val (control, future) = Consumer.committableSource(consumerSetting, Subscriptions.topics("kafkaFile"))
        .map(processMessageSyn)
        .toMat(kafkaSink)(Keep.both)
        .run()*/




     /*For kafkatopic 1 consumer with via option, The below code creates akka stream that will take the kafka consumer taking
      that is subscribed to topic1 as source and calls the processMessage to do some transformations on them and creates producer
      record objects for kafka topic 2 and passes it to via method which will call a Producer flow method that sends messages on
      topic2 through a producer.

      Note: via is only a pipeline that takes and put messages in sink
      */
     val source = Consumer.committableSource(consumerSettings,Subscriptions.topics(config.getString("kafka.topic1")))
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
    /**
      * This method takes messages, changes the values in fifth column and to lower case and all replace empty values with
      * unknown word.
      */
    //  println(msg.record.value())
    val msgVal1 = msg.record.value()
    val msgVal = msgVal1.substring(1, msgVal1.length)
    val msgSplit: Array[String] = msgVal.split("\",\"")
    println(msgSplit.length)
    if (msgSplit.length > 1) {

      if (msgSplit(5).length() == 0) {
        msgSplit(5) = "unknown"
      }
      msgSplit(5) = msgSplit(5).toLowerCase()

    }
    val newMsg = msgSplit.mkString("\",\"")
    println(newMsg)
    Future.successful(ProducerMessage.Message(new ProducerRecord[Array[Byte], String](
      config.getString("kafka.topic2"),
      "\"" + newMsg
    ), msg.committableOffset))
  }
}
object ConsumerActor {
  type Message = CommittableMessage[Array[Byte],String]
  case object consume
}