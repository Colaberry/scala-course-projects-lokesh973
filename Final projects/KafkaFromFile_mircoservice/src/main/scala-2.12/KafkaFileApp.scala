import akka.actor.ActorSystem
import akka.event.slf4j.Logger
import akka.kafka.ProducerSettings
import akka.serialization.ByteArraySerializer
import akka.stream.ActorMaterializer
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

/**
  * Created by lokesh0973 on 2/17/2017.
  *
  * This micro service is built to read data from a file and pass it to kafka that creates a topic 1 and put the messages on
  * topic1.
  *
  */
object KafkaFileApp extends App{

  implicit val system = ActorSystem("KafkaFile")
  implicit val mat = ActorMaterializer.create(system)

  Logger("Creating producer")
  val producerActor =classOf[ProducerActor].getName()

  Logger("Main method")
  akka.Main.main(Array(producerActor))




}
