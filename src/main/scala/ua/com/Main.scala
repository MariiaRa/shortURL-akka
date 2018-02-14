import java.sql.Connection

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import ua.com.actors._
import ua.com.routing.URLRoutes
import ua.com.service.{DBService, UrlService}

import scala.io.StdIn
import scala.reflect.io.{File, Path}

object Main extends URLRoutes{
  import ua.com.DBConfig._

  implicit def system: ActorSystem = ActorSystem("URLShortener-akka")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  val conn = dbConnection
  val urlService = new UrlService()
  val dbService = new DBService(urlService)
  val coordinator: ActorRef = system.actorOf(CoordinatorActor.props(urlService, dbService), "Registrator")

  private val logger = Logger(LoggerFactory.getLogger("Main logger"))
  logger.info("Service started.")
  private def createDBStructure(conn: Connection): Unit = {
    val stmt = conn.createStatement()
    try {
      val sql = """
        create table if not exists urls (
        id int auto_increment primary key,
        short_url varchar(255) not null,
        long_url varchar(255) not null,
        clicks int not null);"""
      stmt.execute(sql)
    } finally {
      stmt.close()
    }
  }

  def main(args: Array[String]): Unit = {

    if(File(Path("db/urlShortener.mv.db")).exists) logger.info("DB exists.")
    else logger.info("Creating DB."); createDBStructure(conn)

    try {
      lazy val URLroutes: Route = routes
      val bindingFuture = Http().bindAndHandle(URLroutes, "localhost", 8080)

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
