import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, MessageEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import ua.com.actors.URLRegistryActor2
import ua.com.entity.InputURL
import ua.com.routing.URLRoutes

class URLRoutesSpec extends WordSpec
  with Matchers
  with ScalaFutures
  with ScalatestRouteTest
  with URLRoutes{

  override implicit val registrator2: ActorRef = system.actorOf(URLRegistryActor2.props, "Registrator")
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
  }
}
