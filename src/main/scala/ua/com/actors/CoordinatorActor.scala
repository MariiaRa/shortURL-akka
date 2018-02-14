package ua.com.actors

import akka.actor._
import ua.com.entity.{InputURL, ShortURL, ValidURL}
import ua.com.service.{DBService, UrlService}

object CoordinatorActor {
case object AllStatsRequest
  case class URLStatsRequest(url: String)
  def props(urlService: UrlService, dbService: DBService): Props = Props(new CoordinatorActor(urlService, dbService))
}

class CoordinatorActor(urlService: UrlService, dbService: DBService) extends Actor with ActorLogging{
import CoordinatorActor._

  def receive: Receive = {
    case a: InputURL =>
      log.info("Received request for shortening")
      if (urlService.validate(a.url)) {
        sender() ! dbService.fetchByLongName(ValidURL(a.url))
      }
    case b: ShortURL => log.info(s"Received access request for short url ${b.url}"); sender() ! dbService.fetchByShortName(b.url)
    case AllStatsRequest => log.info("Received request for url total stats"); sender() ! dbService.getAllStats
    case c: URLStatsRequest => log.info(s"Received request for ${c.url} stats "); sender() ! dbService.getURLStats(ShortURL(c.url))
  }
}