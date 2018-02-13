import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, MessageEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import ua.com.Service.UrlService
import ua.com.actors.URLRegistryActor2
import ua.com.entity.InputURL
import ua.com.routing.URLRoutes

class URLRoutesSpec extends WordSpec
  with Matchers
  with ScalaFutures
  with ScalatestRouteTest
  with URLRoutes{

  lazy val urlService = new UrlService()
  override val registrator2: ActorRef = system.actorOf(URLRegistryActor2.props(urlService), "Registrator")
  lazy val routes = url2Routes

  "urlRoutes" should {
    "be able to add input url and receive a short url name" in {
      val input = InputURL("http://www.google.com")

      val userEntity = Marshal(input).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      val request = Put("/urls").withEntity(userEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)
        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)
      }
    }

    "return stats for all urls in db (GET /stats)" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/urls/stats")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"totalURLCount":7,"totalClickCount":20}""")
      }
    }

    "return stats for one specific url in db (GET /stats/short url name)" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/urls/stats/www.short.com/nJ-RpfRJ")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"clickCount":8}""")
      }
    }

    "return long url from db (GET /short url name)" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/urls/www.short.com/nJ-RpfRJ")

      request ~> routes ~> check {
        status should ===(StatusCodes.Found)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"url":"https://www.flickr.com"}""")
      }
    }
  }
}
