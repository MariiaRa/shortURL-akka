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
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import ua.com.actors.CoordinatorActor.{AllStatsRequest, URLStatsRequest}
import ua.com.entity.{AllStats, InputURL, ShortURL, URLStats}
import ua.com.serializers.JsonSupport

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

trait URLRoutes extends JsonSupport{

  implicit def system: ActorSystem
  implicit val coordinator: ActorRef
  implicit lazy val timeout: Timeout = Timeout(5.seconds)
  private val logger = Logger(LoggerFactory.getLogger("Url routes logger"))

  lazy val routes: Route =
    pathPrefix("urls") { //http://localhost:8080/urls
      concat(
        pathEnd {
          put {
            entity(as[InputURL]) { url =>
              val shortURLCreated: Future[ShortURL] =
                (coordinator ? url).mapTo[ShortURL]
              onComplete(shortURLCreated) {
                case Success(shortURL) => complete(StatusCodes.Created, shortURL)
                case Failure(ex) => logger.error("Invalid url. Please, please provide a valid URL."); complete(400, None)
              }
            }
          }
        },
        pathPrefix("stats") { //http://localhost:8080/urls/stats
          pathEnd {
            get {
              val stats: Future[Option[AllStats]] = (coordinator ? AllStatsRequest).mapTo[Option[AllStats]]
              onSuccess(stats) {
                case Some(statistics) => complete(StatusCodes.OK, statistics)
                case None => complete(StatusCodes.NotFound)
              }
            }
          }~
             get {
               path(Remaining){ url => ////http://localhost:8080/urls/stats/<shortURL>
               val maybeURLStats: Future[Option[URLStats]] =
                (coordinator ? URLStatsRequest(url)).mapTo[Option[URLStats]]
               onSuccess(maybeURLStats) {
                case Some(stats) => complete(StatusCodes.OK, stats)
                case None => complete(StatusCodes.NotFound)
              }
            }
          }
        },
       path(Remaining) { name => //http://localhost:8080/urls/<shortURL>
         get {
           val maybeURL: Future[Option[InputURL]] =
             (coordinator ? ShortURL(name)).mapTo[Option[InputURL]]
             onSuccess(maybeURL) {
               case Some(url) => complete(StatusCodes.OK, url)
               case None => complete(StatusCodes.NotFound)
            }
          }
        }
      )
    }
}
