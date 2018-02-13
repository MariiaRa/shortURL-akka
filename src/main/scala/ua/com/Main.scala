import URLService.UrlShortenerServiceImpl
import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import ua.com.actors._
import ua.com.routing.URLRoutes

import scala.io.StdIn

object Main extends URLRoutes{

  implicit def system: ActorSystem = ActorSystem("URLShortener-akka")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  lazy val urlService = new UrlShortenerServiceImpl()
  /* val validator: ActorRef = system.actorOf(ValidateURLActor.props, "ValidatorActor")
  val urlShortner: ActorRef = system.actorOf(MakeShortURLActor.props, "URLShortnerActor")
  val registrator: ActorRef = system.actorOf(URLRegistryActor.props(urlShortner), "RegistratorActor")
  val coordinator: ActorRef = system.actorOf(CoordinatorActor.props(validator, registrator, urlShortner), "CoordinatorActor")*/

  implicit val registrator2: ActorRef = system.actorOf(URLRegistryActor2.props(urlService), "Registrator")
  def main(args: Array[String]): Unit = {
    /*  if (args.length == 0) println("No URL, please provide a valid URL.")
    else coordinator ! InputURL(args(0))*/
    lazy val routes: Route = url2Routes

    val bindingFuture = Http().bindAndHandle(url2Routes, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
