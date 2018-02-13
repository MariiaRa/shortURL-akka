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
import ua.com.actors.URLRegistryActor2.{AllStatsRequest, URLStatsRequest}
import ua.com.entity.{AllStats, InputURL, ShortURL, URLStats}
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
            }
          }
        },
        pathPrefix("stats") { //http://localhost:8080/urls/stats
          pathEnd {
            get {
              val stats: Future[Option[AllStats]] = (registrator2 ? AllStatsRequest).mapTo[Option[AllStats]]
              onSuccess(stats) {
                case Some(statistics) => complete(StatusCodes.OK, statistics)
                case None => complete(StatusCodes.NotFound)
              }
            }
          }~
             get{
               path(Remaining){ url => ////http://localhost:8080/urls/stats/<shortURL>
              println("request: " + url)
              val maybeURLStats: Future[Option[URLStats]] =
                (registrator2 ? URLStatsRequest(url)).mapTo[Option[URLStats]]
              onSuccess(maybeURLStats) {
                case Some(stats) => complete(StatusCodes.OK, stats)
                case None       => complete(StatusCodes.NotFound)
              }
            }
          }
        },
       path(Remaining) { name =>
          get {
            println("segment: " + name) //http://localhost:8080/urls/<shortURL>
            val maybeURL: Future[Option[InputURL]] =
              (registrator2 ? ShortURL(name)).mapTo[Option[InputURL]]
            /*       rejectEmptyResponse {
              complete(maybeURL)
            }*/
            /*onComplete(maybeURL) {
              case Success(maybeURL) => complete(StatusCodes.Found, maybeURL)
              case Failure(ex) => complete(404, ex.getMessage)
            }*/

            onSuccess(maybeURL) {
              case Some(url) => complete(StatusCodes.Found, url)
              case None       => complete(StatusCodes.NotFound)
            }
          }
        }
      )
    }
}
