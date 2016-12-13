package karls

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{Sink, Source}
import com.hunorkovacs.koauth.domain.KoauthRequest
import com.hunorkovacs.koauth.service.consumer.DefaultConsumerService

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Main extends App with TweetService with AkkaContext with Config {

  private val consumer = new DefaultConsumerService(system.dispatcher)

  val body = s"track=${twitterConfig.track.replaceAll("\\s", "%20")}"

  val oauthHeader: Future[String] = consumer.createOauthenticatedRequest(
    KoauthRequest(
      method = "POST",
      url = s"https://${twitterConfig.host}${twitterConfig.path}",
      authorizationHeader = None,
      body = Some(body)
    ),
    twitterConfig.consumerKey,
    twitterConfig.consumerSecret,
    twitterConfig.accessToken,
    twitterConfig.accessTokenSecret
  ).map(_.header)

  oauthHeader.onComplete {

    case Success(header) =>

      val httpHeaders: List[HttpHeader] = List(
        HttpHeader.parse("Authorization", header) match {
          case ParsingResult.Ok(h, _) => Some(h)
          case _ => None
        },
        HttpHeader.parse("Accept", "*/*") match {
          case ParsingResult.Ok(h, _) => Some(h)
          case _ => None
        }
      ).flatten


      val httpRequest: HttpRequest = HttpRequest(
        method = HttpMethods.POST,
        uri = twitterConfig.path,
        headers = httpHeaders,
        entity = HttpEntity(MediaTypes.`application/x-www-form-urlencoded`.toContentType(HttpCharsets.`UTF-8`), body)
      )

      Source.single(httpRequest)
        .via(Http().outgoingConnectionHttps(twitterConfig.host))
        .runWith(Sink.head)
        .onComplete {
          case Success(response) if response.status.intValue == 200 =>
            val tweetSource = response.entity.dataBytes
            Http().bindAndHandle(routes(tweetSource), interface, port)
          case Success(response) =>
            println(s"Unexpected response: $response")
          case Failure(e) =>
            println("Twitter call failed")
            e.printStackTrace()
        }

    case Failure(e) =>
      println("Could not create HTTP headers")
      e.printStackTrace()
  }
}