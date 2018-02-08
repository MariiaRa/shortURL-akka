import akka.actor._
import akka.http.scaladsl.server.Route
import ua.com.actors._
import ua.com.entity.InputURL
import ua.com.routing.URLRoutes
import akka.http.scaladsl.Http

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn
object Main extends URLRoutes{

  def main(args: Array[String]): Unit = {
    // Create the 'URLShortener' actor system
    val system = ActorSystem("URLShortener-akka")
    /* implicit val materializer: ActorMaterializer = ActorMaterializer()*/

    val validator: ActorRef = system.actorOf(ValidateURLActor.props, "ValidatorActor")
    val urlShortner: ActorRef = system.actorOf(MakeShortURLActor.props, "URLShortnerActor")
    val registrator: ActorRef = system.actorOf(URLRegistryActor.props(urlShortner), "RegistratorActor")

    val coordinator: ActorRef = system.actorOf(CoordinatorActor.props(validator, registrator, urlShortner), "CoordinatorActor")

    if (args.length == 0) println("No URL, please provide a valid URL.")
    else coordinator ! InputURL(args(0))


    lazy val routes: Route = urlRoutes

    val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate())


  }
}
