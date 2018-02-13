import java.sql.Connection

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import ua.com.Service.UrlService
import ua.com.actors._
import ua.com.routing.URLRoutes

import scala.io.StdIn

object Main extends URLRoutes{
  import ua.com.DBConfig._

  implicit def system: ActorSystem = ActorSystem("URLShortener-akka")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  val conn = dbConnection
  val urlService = new UrlService()
  val registrator2: ActorRef = system.actorOf(URLRegistryActor2.props(urlService), "Registrator")

  private def createDbStructure(conn: Connection): Unit = {
   // val sql2: String = "create INDEX ind_short on urls(short_url);"
    val stmt = conn.createStatement()
    try {
      //drop schema urlShortener2
      //create schema if not exists urlShortener2;
      //set schema urlShortener2;
      val sql1 = """
        create table if not exists urls (
        id int auto_increment primary key,
        short_url varchar(255) not null,
        long_url varchar(255) not null,
        clicks int not null);"""
      stmt.execute(sql1)
    //  stmt.execute(sql2)
      println("DB created")
    } finally {
      stmt.close
    }
  }

  def main(args: Array[String]): Unit = {
    try {
      lazy val routes: Route = url2Routes
      val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)

      println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
      StdIn.readLine() // let it run until user presses return
      bindingFuture
        .flatMap(_.unbind()) // trigger unbinding from the port
        .onComplete(_ => system.terminate()) // and shutdown when done
    } finally {
      conn.close()
    }
  }
}
