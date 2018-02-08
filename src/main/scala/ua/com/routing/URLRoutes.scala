package ua.com.routing

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.{as, entity}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.put
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import ua.com.entity.{InputURL, ShortURL}
import ua.com.serializers.JsonSupport

import scala.concurrent.Future

trait URLRoutes extends JsonSupport{
  implicit def system: ActorSystem

  def coordinator: ActorRef

  lazy val urlRoutes: Route =

    path("users") { //http://localhost:8080/users
      put {
        entity(as[InputURL]) { url =>
          val shortURLCreated: Future[ShortURL] =
            (coordinator ? url).mapTo[ShortURL]
          complete(shortURLCreated)
        }
      }
    }

}
