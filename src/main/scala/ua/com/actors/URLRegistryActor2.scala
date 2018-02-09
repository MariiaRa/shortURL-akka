package ua.com.actors

import java.util.regex.Pattern

import akka.actor.{Actor, ActorLogging, Props}
import ua.com.entity.{InputURL, ShortURL}


object URLRegistryActor2 {

  private def findKeyByValue(map: collection.mutable.Map[String, String], value: String): String = {
    map.filter{ case (k, v) => v == value }.keys.mkString}

    val urlPattern = "([A-Za-z]{3,9}:(?:\\/\\/)?)+([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*" +
      "((?!(10\\.|172\\.(1[6-9]|2\\d|3[01])\\.|192\\.168\\.).*)(?!255\\.255\\.255\\.255)" +
      "(25[0-5]|2[1,3]\\d|[1]\\d\\d|[1-9]\\d|[1-9])(\\.(25[0-5]|2[0-4]\\d|[1]\\d\\d|[1-9]\\d|\\d)){3}|" +
      "[^\\.\\-\\s]([a-zA-Z0-9-]+\\.)*[a-zA-Z0-9-]+\\." +
      "(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{1,9})|" +
      "(\\[(([\\da-fA-F]{4}(:|::)){1}([\\da-fA-F]{1,4}(:|::)){1,6}([:]{1,2})?" +
      "([\\da-fA-F]{1,4}){1})\\])|((?:\\+?(\\d{1}))?[- (](\\d{3})[- )](\\d{3})[- ](\\d{1,4})))(:[0-9]+)*" +
      "(\\/($|[a-zA-Z0-9\\.,\\?'\\\\\\+&%$#=~_\\-\\(\\)]+)?)*"

    def isValid(url: String): Boolean = Pattern.matches(urlPattern, url)
  val urlMap = collection.mutable.Map[String, String]()
    val alphabet: String = "bcdfghjkmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ23456789-_"
    val randomStringLength = 8

    private def randomAlphaNumericString(chars: String): String = {
      val sb = new StringBuilder
      for (i <- 1 to randomStringLength) {
        val randomNum = util.Random.nextInt(chars.length)
        sb.append(chars(randomNum))
      }
      sb.toString
    }

    def props: Props = Props[URLRegistryActor2]
  }

class URLRegistryActor2 extends Actor with ActorLogging {
  import URLRegistryActor2._

  def receive: Receive = {

    case a: InputURL => {
      if (isValid(a.url)) {
        if (urlMap.exists(_._2 == a.url)) {
          val short = findKeyByValue(urlMap, a.url)
          sender() ! ShortURL(short)
        }else {
          val shortName = "www."+ randomAlphaNumericString(alphabet)
          urlMap += shortName -> a.url
          println(urlMap.toString)
          sender() ! ShortURL(shortName)
        }
      }
    }
    case b: ShortURL =>
      val inputURL = urlMap(b.url)
      println("input: "+ inputURL)
sender() ! InputURL(inputURL)
  }
}
