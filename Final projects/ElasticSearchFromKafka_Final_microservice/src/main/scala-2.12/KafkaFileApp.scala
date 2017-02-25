import akka.actor.ActorSystem
import akka.event.slf4j.Logger
import akka.kafka.ProducerSettings
import akka.serialization.ByteArraySerializer
import akka.stream.ActorMaterializer
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

/**
  * Created by lokesh0973 on 2/17/2017.
  *
  * In this project, creating a micro service that reads from the kafka on a topic specified and writes the data
  * to Elastic search.
  */
object KafkaFileApp extends App{

  implicit val system = ActorSystem("KafkaFile")
  implicit val mat = ActorMaterializer.create(system)

  Logger("Creating searchActor")
  val elasticSearch =classOf[ElasticSearchActor].getName()

  Logger("Main method")
  akka.Main.main(Array(elasticSearch))




}
