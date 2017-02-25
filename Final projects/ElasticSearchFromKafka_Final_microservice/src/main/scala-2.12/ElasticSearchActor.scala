import java.io.IOException
import java.util

import ElasticSearchActor.start
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
  *
  * This class creates a Stream that takes initialized source with kafka consumer and raise http requests for each message it
  * gets from the consumer.
  */
class ElasticSearchActor extends Actor{
  implicit val system =  ActorSystem()
  implicit val mat = ActorMaterializer()
  val config = ConfigFactory.load()

  //Calling next only once to get the first record(headers) from the file.
  val headers = scala.io.Source.fromFile(
    config.getString("inputFile.filename")).getLines()
  .next()


  override def preStart(): Unit = {
    super.preStart()
    self ! start
  }
  override def receive: Receive = {
    case start=>{

      Logger("Initializing consumer setting")
      val consumerSetting = ConsumerSettings(
        context.system,new ByteArrayDeserializer, new StringDeserializer)
        .withBootstrapServers(config.getString("kafka.broker-list"))
        .withGroupId(config.getString("kafka.topic2"))
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest")
      //Akka stream to raise http request
      val done = Consumer.committableSource(consumerSetting,Subscriptions.topics(config.getString("kafka.topic2")))
        .mapAsync(5)(processMessage)
        .via(connectionFlow)
        .runWith(Sink.ignore)

    }
  }
  def connectionFlow:Flow[HttpRequest, HttpResponse, Any] = {
    //Connecting to the elastic search, hostname and port refers to elastic search
    Http().outgoingConnection(config.getString("http.interface"), config.getInt("http.port"))

  }
  def processMessage(msg:ElasticSearchActor.Message): Future[HttpRequest] = {
    println(msg.record.value())
    val request = requestHttp(msg)
    Future.successful(request)
  }
  def createJSONValueStr(str:String):String = {
    //Handling the values for JSON, this method removes "" and add \" which is needed for JSON and also put default
    //values for numeric types
    if(str.indexOf("\"").equals(-1))
      if(str.length()==0) "-1" else str
    else
      "\""+str.replace("\"","")+"\""
  }
  def requestHttp(msg:ElasticSearchActor.Message) ={
    /**
      * We are using the delimiter based on regex. This will read csv file correctly.
      * In our case the csv files has string columns enclosed with "", which inturn can have ,
      * Also the integer columns will not have "". Below regex will handle all these conditions and split fields.
      * We can have simpler format for header but using same for both.
      */
    val msgArray = msg.record.value().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1)
        //println("headers "+headers.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1).toList)
    val mapHeaders = headers.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1).toList zip msgArray.toList
    //
    // "\""+a._1.replace("\"","")+"\":\""+a._2.replace("\"","")+"\""
    //Forming key:value pair text for JSON
    val mapStrings = mapHeaders.map(a=>
      "\""+a._1.replace("\"", "")+"\":"+createJSONValueStr(a._2)+""
      )
    //Building JSON strin
    val jsonString = "{"+mapStrings.mkString(",")+"}"
    // ID to specify for elastic search, each row should have unique id
    val id = msgArray(0).replace("\"","")

   /* For Testing purpose with dummy data
 val request: HttpRequest = RequestBuilding.Put(s"testing/$id")

    val entity = HttpEntity(ContentTypes.`application/json`,s"{data: $msg.record.value()}")
    request.withEntity(entity)

   val dataStr = "{\"data\": \""+value+"\"}"
    val data = ByteString(dataStr)
*/
    //Building request to send messages to elastic search
    val request = HttpRequest(
      POST,
      uri = config.getString("elastic.uri")+"/"+id,
      entity = HttpEntity(ContentTypes.`application/json`, ByteString(jsonString) )
      )
    println(request)
    request
  }
}
object ElasticSearchActor {
  type Message = CommittableMessage[Array[Byte],String]
  case object start

}