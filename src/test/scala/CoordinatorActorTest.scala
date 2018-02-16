import CoordinatorActor.{AllStatsRequest, URLStatsRequest}
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.specs2.mutable.SpecificationLike
import ua.com.DBConfig.dbConnection
import ua.com.entity._
import ua.com.service.{DBService, UrlService}

object CoordinatorActor {

  case object AllStatsRequest

  case class URLStatsRequest(url: String)

  def props(urlService: UrlService, dbService: DBService): Props =
    Props(new CoordinatorActor(urlService, dbService))
}

class CoordinatorActor(urlService: UrlService, dbService: DBService) extends Actor with ActorLogging {

  import CoordinatorActor._

  def receive: Receive = {
    case a: InputURL =>
      log.info("Received request for shortening")
      if (urlService.validate(a.url)) {
        sender() ! dbService.fetchByLongName(ValidURL(a.url))
      }
    case b: ShortURL =>
      log.info(s"Received access request for short url ${b.url}")
      sender() ! dbService.fetchByShortName(b.url)
    case AllStatsRequest =>
      log.info("Received request for url total stats")
      sender() ! dbService.getAllStats
    case c: URLStatsRequest =>
      log.info(s"Received request for ${c.url} stats ")
      sender() ! dbService.getURLStats(ShortURL(c.url))
  }
}

class CoordinatorActorTest extends TestKit(ActorSystem()) with ImplicitSender with SpecificationLike {
  implicit val executionContext = system.dispatcher
  val conn = dbConnection
  val urlService = new UrlService()
  val dbService = new DBService(urlService)

  "An CoordinatorActorTest actor" should {

    "send back string message" in {

      val testActor = system.actorOf(Props(new CoordinatorActor(urlService, dbService)))
      val msg1 = InputURL("http://www.google.com")
      val msg2 = ShortURL("www.short.com/dMZXgD63")
      val msg3 = AllStatsRequest
      val msg4 = URLStatsRequest("www.short.com/dQ7SLbF_")
      testActor ! msg1
      val response1 = expectMsgType[ShortURL]
      testActor ! msg2
      val response2 = expectMsgType[Option[InputURL]]
      testActor ! msg3
      val response3 = expectMsgType[Option[AllStats]]
      testActor ! msg4
      val response4 = expectMsgType[Option[URLStats]]
      success
    }
  }


}
