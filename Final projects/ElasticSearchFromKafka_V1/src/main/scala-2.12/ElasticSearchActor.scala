import java.io.IOException
import java.util

import akka.Done
import akka.actor.{Actor, ActorSystem}
import akka.actor.Actor.Receive
import akka.event.slf4j.Logger
import akka.http.javadsl.model
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffsetBatch}
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import HttpMethods._
import akka.util.ByteString

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, _}
import scala.concurrent.Future
import scala.util.{Failure, Success}
/**
  * Created by lokesh0973 on 2/19/2017.
  */
class ElasticSearchActor extends Actor{
  implicit val system =  ActorSystem()
  implicit val mat = ActorMaterializer()
  val config = ConfigFactory.load()

  val headers = scala.io.Source.fromFile(
  "c:\\scala\\scala-course-projects-lokesh973\\projects\\1000-genomes_other_sample_info_sample_info.csv").getLines()
  .next()

  val indexName = "genome"
  override def preStart(): Unit = {
    super.preStart()
  }
  override def receive: Receive = {
    case start=>{
      println("Sending Http requests")
      val consumerSetting = ConsumerSettings(
        context.system,new ByteArrayDeserializer, new StringDeserializer)
        .withBootstrapServers("localhost:9092")
        .withGroupId("kafkatopic2")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest")
      val done = Consumer.committableSource(consumerSetting,Subscriptions.topics("kafkatopic2"))
        .mapAsync(5)(processMessage)
        .via(connectionFlow)
        .runWith(Sink.ignore)

    }
  }
  def connectionFlow:Flow[HttpRequest, HttpResponse, Any] = {
    //Http().outgoingConnection("192.168.99.100", 9200)
    Http().outgoingConnection(config.getString("http.interface"), config.getInt("http.port"))

  }
  def processMessage(msg:ElasticSearchActor.Message): Future[HttpRequest] = {
    val request = requestHttp(msg)
    Future.successful(request)
  }
  def createJSONValueStr(str:String):String = {
    //println("String "+str+" index "+str.indexOf("\""))
    if(str.indexOf("\"").equals(-1))
      if(str.length()==0) "-1" else str
    else
      "\""+str.replace("\"","")+"\""
  }
  def requestHttp(msg:ElasticSearchActor.Message) ={

    val msgArray = msg.record.value().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1)
        //println("headers "+headers.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1).toList)
    val mapHeaders = headers.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1).toList zip msgArray.toList
    //
    // "\""+a._1.replace("\"","")+"\":\""+a._2.replace("\"","")+"\""
    val mapStrings = mapHeaders.map(a=>
      "\""+a._1.replace("\"", "")+"\":"+createJSONValueStr(a._2)+""
      )

    val jsonString = "{"+mapStrings.mkString(",")+"}"

    val id = msgArray(0).replace("\"","")

   /* For Testing purpose with dummy data
 val request: HttpRequest = RequestBuilding.Put(s"testing/$id")

    val entity = HttpEntity(ContentTypes.`application/json`,s"{data: $msg.record.value()}")
    request.withEntity(entity)

   val dataStr = "{\"data\": \""+value+"\"}"
    val data = ByteString(dataStr)
*/

    val request = HttpRequest(
      POST,
      uri = s"/testing8/test/$id",
      entity = HttpEntity(ContentTypes.`application/json`, ByteString(jsonString) )
      )
    println(request)
    request
  }
}
object ElasticSearchActor {
  type Message = CommittableMessage[Array[Byte],String]
  case object start
  case class test()

}