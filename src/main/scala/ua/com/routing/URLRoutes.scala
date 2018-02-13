package ua.com.routing

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{get, put}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import ua.com.entity.{InputURL, ShortURL}
import ua.com.serializers.JsonSupport

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

trait URLRoutes extends JsonSupport{

  implicit def system: ActorSystem
  implicit val registrator2: ActorRef
  implicit lazy val timeout = Timeout(5.seconds)

 /* lazy val urlRoutes: Route =
    path("users") { //http://localhost:8080/urls
      put {
        entity(as[InputURL]) { url =>
          val shortURLCreated: Future[ShortURL] =
            (coordinator ? url).mapTo[ShortURL]
          complete(shortURLCreated)
        }
      }
    }*/

  lazy val url2Routes: Route =
    pathPrefix("urls") { //http://localhost:8080/urls
      concat(
        pathEnd {
          put {
            entity(as[InputURL]) { url =>
              val shortURLCreated: Future[ShortURL] =
                (registrator2 ? url).mapTo[ShortURL]
              onComplete(shortURLCreated) {
                case Success(shortURLCreated) => complete(StatusCodes.Created, shortURLCreated)
                case Failure(ex) => println("Invalid url. Please, please provide a valid URL."); complete(400, None)
              }
              /*onSuccess(shortURLCreated) { created =>
                complete(StatusCodes.Created, created)
              }*/
            }
          }
        },
        path(Remaining) { name =>
          get {
            println("segment: " + name) //http://localhost:8080/urls/<shortURL>
            val maybeURL: Future[InputURL] =
              (registrator2 ? ShortURL(name)).mapTo[InputURL]
            /*       rejectEmptyResponse {
              complete(maybeURL)
            }*/
            onComplete(maybeURL) {
              case Success(maybeURL) => complete(StatusCodes.Found, maybeURL)
              case Failure(ex) => complete(404, ex.getMessage)
            }
          }

        }
      )
    }
}
