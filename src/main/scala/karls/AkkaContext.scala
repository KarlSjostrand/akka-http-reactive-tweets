package karls

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

trait AkkaContext {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executor = system.dispatcher
}

object AkkaContext extends AkkaContext
