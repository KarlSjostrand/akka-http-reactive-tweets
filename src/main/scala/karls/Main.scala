package karls

import java.net.URLEncoder

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{Sink, Source}
import com.hunorkovacs.koauth.domain.KoauthRequest
import com.hunorkovacs.koauth.service.consumer.DefaultConsumerService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Main extends App with TweetService with Config {

  import AkkaContext._

  val body = s"track=${URLEncoder.encode(twitterConfig.track, "utf-8")}"

  val futureTweetStream = createHeaders(twitterConfig, body).flatMap { httpHeaders =>

    val httpRequest = HttpRequest(
      method = HttpMethods.POST,
      uri = twitterConfig.path,
      headers = httpHeaders,
      entity = HttpEntity(MediaTypes.`application/x-www-form-urlencoded`.toContentType(HttpCharsets.`UTF-8`), body)
    )

    Source.single(httpRequest)
      .via(Http().outgoingConnectionHttps(twitterConfig.host))
      .runWith(Sink.head)
      .map(_.entity.dataBytes)

  }

  futureTweetStream.onComplete {
    case Success(tweetStream) =>
      Http().bindAndHandle(routes(tweetStream), interface, port)
    case Failure(e) =>
      e.printStackTrace()
  }


  def createHeaders(twitterConfig: TwitterConfig, body: String)(implicit executor: ExecutionContext): Future[List[HttpHeader]] = {
    val consumer = new DefaultConsumerService(executor)

    val koauthRequest = KoauthRequest(method = "POST",
      url = s"https://${twitterConfig.host}${twitterConfig.path}",
      body = Some(body))

    consumer.createOauthenticatedRequest(
      koauthRequest,
      twitterConfig.consumerKey,
      twitterConfig.consumerSecret,
      twitterConfig.accessToken,
      twitterConfig.accessTokenSecret
    )
      .map(_.header)
      .map { header =>
        List(
          HttpHeader.parse("Authorization", header) match {
            case ParsingResult.Ok(h, _) => Some(h)
            case _ => None
          },
          HttpHeader.parse("Accept", "*/*") match {
            case ParsingResult.Ok(h, _) => Some(h)
            case _ => None
          }
        ).flatten
      }
  }


}