import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.serialization.ByteArraySerializer
import akka.stream.ActorMaterializer
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

/**
  * Created by lokesh0973 on 2/17/2017.
  */
object KafkaFileApp extends App{

  implicit val system = ActorSystem("KafkaFile")
  implicit val mat = ActorMaterializer.create(system)

  println("Creating producer")
  val producerActor =classOf[ProducerActor].getName()
  println("Creating consumer")
  val consumerActor = classOf[ConsumerActor].getName()
  println("Main method")
  akka.Main.main(Array(producerActor))


  println("Entering KafkaFileApp")

}
