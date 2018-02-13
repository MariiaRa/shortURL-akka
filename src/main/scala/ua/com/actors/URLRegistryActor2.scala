package ua.com.actors

import akka.actor._
import ua.com.Service.UrlService
import ua.com.entity.{InputURL, ShortURL, ValidURL}

object URLRegistryActor2 {
case object AllStatsRequest
  case class URLStatsRequest(url: String)

  // def props: Props = Props[URLRegistryActor2]
  def props(urlService: UrlService): Props = Props(new URLRegistryActor2(urlService))
}

class URLRegistryActor2(urlService: UrlService) extends Actor {
import URLRegistryActor2._
  def receive: Receive = {
    case a: InputURL => {
      if (urlService.validate(a.url)) {
        sender() ! urlService.fetchByLong(ValidURL(a.url))
      }
    }
    case b: ShortURL =>
      sender() ! urlService.fetchByShort(b.url)
    case AllStatsRequest => sender() ! urlService.getAllStats
    case c: URLStatsRequest => sender() ! urlService.getURLStats(ShortURL(c.url))
      }
}