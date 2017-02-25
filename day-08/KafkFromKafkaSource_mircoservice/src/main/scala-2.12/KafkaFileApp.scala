import akka.actor.ActorSystem
import akka.event.slf4j.Logger
import akka.kafka.ProducerSettings
import akka.serialization.ByteArraySerializer
import akka.stream.ActorMaterializer
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

/**
  * Created by lokesh0973 on 2/17/2017.
  *
  * This microservice will subscribe to topic1 in kafka and do transformations on the message and send them to
  * a producer which will send these messages to topic2. This scala object is entry point to this microservice which
  * gives the parent actor class name to akka main.
  */
object KafkaFileApp extends App{

  implicit val system = ActorSystem("KafkaFile")
  implicit val mat = ActorMaterializer.create(system)

  Logger("Creating consumer")
  //Create reference for customer actor calls
  val consumerActor =classOf[ConsumerActor].getName()

  Logger("Main method")
  //Calling the Akka main method, to initialize actor system
  akka.Main.main(Array(consumerActor))




}
