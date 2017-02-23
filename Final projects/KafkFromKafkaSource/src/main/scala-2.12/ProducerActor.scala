import ProducerActor.stopProducer
import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.slf4j.Logger
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success


/**
  * Created by lokesh0973 on 2/18/2017.
  */
class ProducerActor extends Actor with ActorLogging{
 implicit val mat = ActorMaterializer()
  implicit val system = ActorSystem()

  override def preStart(): Unit = {
    super.preStart()
    Logger("self start")

    self ! startProducer
    Thread.sleep(1000)
    val consumerActor = context.actorOf(Props[ConsumerActor], "consumer" )
    consumerActor ! ConsumerActor.consume
  }

  override def postStop(): Unit = {
    context.system.terminate()
      .onComplete({
      case _=>system.terminate()
    })

  }
  override def receive: Receive = {
    case ProducerActor.startProducer => {

      val genomeFile = scala.io.Source.fromFile(
        KafkaConfig.fileName)
      var count=0
      val iterator: Iterator[Unit] = genomeFile.getLines().map({
        count=count+1
        elem => println("iterator value "+elem)
      })

      println("count1 "+count)

      Thread.sleep(200)

      println("count2 "+count)


      val producerSettings = ProducerSettings(context.system, new ByteArraySerializer, new StringSerializer)
        .withBootstrapServers("localhost:9092")
      val kafkaSink = Producer.plainSink(producerSettings)//Producer.plainSink(producerSettings)





     val source: Source[ProducerRecord[Array[Byte], String], NotUsed] = Source
       .fromIterator(() => genomeFile.getLines()).map{new ProducerRecord[Array[Byte], String](KafkaConfig.topic1, _)}
    val done: Future[Done] = source.runWith(kafkaSink)


    //To call onComplete on Future

    done.onComplete({
      _ => {
        self ! ProducerActor.stopProducer
        //context.stop(self)
      }
    })

/*
     // Override onComplete of Future
      done.onComplete  {
        case Success(s)=> {
            self ! stopProducer
        }
      }
*/
     /* done.onSuccess _ = {
        case Success() => context.stop(self)
      }*/
   //  val (control,future) = source.toMat(kafkaSink)(Keep.both)


    /*  val foreach = FileIO.fromPath(Paths.get("c:\\scala\\scala-course-projects-lokesh973\\projects\\1000-genomes_other_sample_info_sample_info.csv"))
        .to(kafkaSink)
        .run()*/

     }
    case ProducerActor.stopProducer =>{
      context.stop(self)
    }

  }

}

object ProducerActor{
  case object startProducer;
  case object stopProducer;
}
