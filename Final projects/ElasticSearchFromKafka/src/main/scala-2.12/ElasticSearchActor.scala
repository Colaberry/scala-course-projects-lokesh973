import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.http.scaladsl.Http
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffsetBatch}
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.{Keep, Sink}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.Future

/**
  * Created by lokesh0973 on 2/19/2017.
  */
class ElasticSearchActor extends Actor{
  val indexName = "genome"
  override def receive: Receive = {
    case start=>{
      val consumerSetting = ConsumerSettings(context.system,new ByteArrayDeserializer, new StringDeserializer)
        .withBootstrapServers("localhost:9092")
        .withGroupId("kafkatopic2")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest")
      val (control,future) = Consumer.committableSource(consumerSetting,Subscriptions.topics("kafkatopic2"))
        .mapAsync(1)(processMessage)
        .map(_.committableOffset)
        .groupedWithin(10, 15 seconds)
        .map(group=>group.foldLeft(CommittableOffsetBatch.empty){(batch,elem)=>batch.updated(elem)})
        .mapAsync(1)(_.commitScaladsl())
        .toMat(Sink.ignore)(Keep.both)
        .run()
    }
  }
  def processMessage(msg:ElasticSearchActor.Message): Future[ElasticSearchActor.Message] = {

    requestHttp(msg)
    Future.successful(msg)
  }
  def requestHttp(msg:ElasticSearchActor.Message) = {
    Http(context.system).
      singleRequest(createRequest).
      flatMap {
        case resp if resp.status.isSuccess =>
          Unmarshal(resp.entity).to[ElasticSearchActor.test]
        case resp =>
          resp.discardEntityBytes()
          Future.failed(new RuntimeException(s"Unexpected status code of: ${resp.status}"))
      }
  }
  def createRequest = {

  }
}
object ElasticSearchActor {
  type Message = CommittableMessage[Array[Byte],String]
  case object start
  case class test()
}