import java.io.{BufferedReader, FileReader, InputStreamReader}
import java.nio.file.{Files, Paths}

import ProducerActor.stopProducer
import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorLogging, Cancellable, Props}
import akka.actor.Actor.Receive
import akka.kafka.ConsumerMessage.Committable
import akka.kafka.ProducerMessage.Message
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.impl.fusing.Map
import akka.stream.scaladsl.Source.fromIterator
import akka.stream.scaladsl.{FileIO, Keep, RunnableGraph, Sink, Source}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import org.omg.PortableInterceptor.SUCCESSFUL

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.concurrent.duration.Duration
import scala.util.{Random, Success}


/**
  * Created by lokesh0973 on 2/18/2017.
  */
class ProducerActor extends Actor with ActorLogging{
 implicit val mat = ActorMaterializer()

  override def preStart(): Unit = {
    super.preStart()
    println("self start")
    self ! startProducer
    Thread.sleep(1000)
    val consumerActor = context.actorOf(Props[ConsumerActor], "consumer" )
    consumerActor ! ConsumerActor.consume
  }
  override def receive: Receive = {
    case startProducer => {

      val genomeFile = scala.io.Source.fromFile(
        "c:\\scala\\scala-course-projects-lokesh973\\projects\\1000-genomes_other_sample_info_sample_info.csv")

      val producerSettings = ProducerSettings(context.system, new ByteArraySerializer, new StringSerializer)
        .withBootstrapServers("localhost:9092")
      val kafkaSink = Producer.plainSink(producerSettings)//Producer.plainSink(producerSettings)





     val source: Source[ProducerRecord[Array[Byte], String], NotUsed] = Source.fromIterator(() => genomeFile.getLines().toIterator).map{new ProducerRecord[Array[Byte], String]("kafkaFile", _)}
/*     val done: Future[Done] = source.runWith(kafkaSink)

      done.onComplete {
        case _ => self ! stopProducer
      }*/

     /* done.onSuccess _ = {
        case Success() => context.stop(self)
      }*/
     val (control,future) = source.toMat(kafkaSink)(Keep.both).run()



//      future.onComplete = {
//        case _ => println("Done")
//      }

//
//     val foreach = FileIO.fromPath(Paths.get("c:\\scala\\scala-course-projects-lokesh973\\projects\\1000-genomes_other_sample_info_sample_info.csv"))
//        .to(kafkaSink)
//        .run()

     }

  }

}

object ProducerActor{
  case object startProducer;
  case object stopProducer;
}
