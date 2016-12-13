package karls

import akka.http.scaladsl.model.ws._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ThrottleMode.Shaping
import akka.stream.scaladsl.{BroadcastHub, Flow, Framing, Keep, Sink, Source}
import akka.util.ByteString

import scala.concurrent.duration._

trait TweetService extends AkkaContext {

  protected def routes(tweetSource: Source[ByteString, Any]): Route = {

    val tweets = tweetSource
      .via(Framing.delimiter(ByteString("\r\n"), 65536))
      .map(_.utf8String)
      .throttle(1, 2.seconds, 1, Shaping)
      .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.right)
      .run()

    val tweetsFlow = Flow.fromSinkAndSource(Sink.ignore, tweets.map(TextMessage(_)))

    get {
      path("tweets") {
        handleWebSocketMessages(tweetsFlow)
      } ~ pathSingleSlash {
        getFromResource("web/index.html")
      } ~ getFromResourceDirectory("web")
    }
  }

}