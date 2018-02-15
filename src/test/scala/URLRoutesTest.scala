import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, MessageEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import ua.com.entity.InputURL
import ua.com.routing.URLRoutes
import ua.com.service.{DBService, UrlService}

class URLRoutesTest extends WordSpec
  with Matchers
  with ScalaFutures
  with ScalatestRouteTest
  with URLRoutes{

  lazy val urlService = new UrlService()
  lazy val dbService = new DBService(urlService)
  override val coordinator: ActorRef = system.actorOf(CoordinatorActor.props(urlService, dbService), "Registrator")
  lazy val URLroutes: Route = routes

  "urlRoutes" should {
    "be able to add input url and receive a short url name" in {
      val input = InputURL("http://www.google.com")

      val userEntity = Marshal(input).to[MessageEntity].futureValue
      val request = Put("/urls").withEntity(userEntity)

      request ~> URLroutes ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
      }
    }

    "return stats for all urls in db (GET /stats)" in {

      val request = HttpRequest(uri = "/urls/stats")

      request ~> URLroutes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"totalURLCount":17,"totalClickCount":67223}""")
      }
    }

    "return stats for one specific url in db (GET /stats/short url name)" in {
      val request = HttpRequest(uri = "/urls/stats/www.short.com/dMZXgD63")

      request ~> URLroutes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"clickCount":25}""")
      }
    }

    "return long url from db (GET /short url name)" in {
      val request = HttpRequest(uri = "/urls/www.short.com/dMZXgD63")

      request ~> URLroutes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"url":"http://www.dw.com"}""")
      }
    }
  }
}
