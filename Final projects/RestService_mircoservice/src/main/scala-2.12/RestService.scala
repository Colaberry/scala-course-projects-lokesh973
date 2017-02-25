import java.io.{File, IOException}
import java.lang.Iterable
import java.nio.file.Path

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.http.javadsl.marshallers.jackson.Jackson
import akka.http.javadsl.model.ContentType.NonBinary
import akka.http.javadsl.model.headers.HttpCredentials
import akka.http.javadsl.model._
import akka.http.javadsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest}
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import akka.http.scaladsl.model.StatusCodes._

import scala.concurrent.ExecutionContext.Implicits.global
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future


/**
  * Created by lokesh0973 on 2/23/2017.
  */

object RestService extends App with DefaultJsonProtocol{
  case class search()
  case class fuzzy()
  case class searchInfo(a:String,b:String)
  val config = ConfigFactory.load()
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()


  def ipApiConnectionFlow:Flow[HttpRequest,HttpResponse,Future[Any]] = {
    Http().outgoingConnection(config.getString("elastic.hostName"),config.getInt("elastic.port"))
  }
  def fetchFromElastic(term:String, caseVal:Any): Future[Either[String, String]] = {
    Source.single(buildRequest(term, caseVal)).via(ipApiConnectionFlow).runWith(Sink.head).flatMap { response =>
      response.status match {
        case OK => Future.successful(Right(response.entity.toString()))
        case BadRequest => Future.failed(new Exception("Incorrect query"))
        case _ => Future.failed(new Exception("Incorrect query"))
      }
    }
  }
  def buildRequest(term:String, caseVal:Any): HttpRequest = {
    caseVal match  {
      case _:search =>   {
        println("I am in search"+term)
        val request = RequestBuilding.Get(config.getString("elastic.uri")+"/"+term)
        println(request)
        request
      }
      case _:fuzzy => {
        println("I am in fuzzy"+term)
        val fuzzyString = "{\"query\":{\"fuzzy\":{\"sample\":\""+term+"\"}}}"
        val request = HttpRequest(
          POST,
          uri = config.getString("elastic.uri")+"/_search",
          entity = HttpEntity(ContentTypes.`application/json`, ByteString(fuzzyString) )
        )
        println(request)
        request
      }

    }

  }

  val routes=  {
    pathPrefix("genome") {
      (get & path("search" / Segment)) { term =>
        complete {
          val aa: Future[Either[String, String]] = fetchFromElastic(term, search())
          aa.map[String] {
            case Right(s) => s
           // case Left(errorMessage) => Future["Error"]
          }

        }
      }~
      (get & path("fuzzy" / Segment)) { term =>
        complete {
          val aa: Future[Either[String, String]] = fetchFromElastic(term, fuzzy())
          aa.map[String] {
            case Right(s) => s
            // case Left(errorMessage) => Future["Error"]
          }

        }
      }
    }
  }
 /* val routes= {
    (get & path(Segment)){
      term => complete{
        "AAAA"+term
      }
    }
  }*/
  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))

}
