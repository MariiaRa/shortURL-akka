package URLService

import java.util.regex.Pattern

import ua.com.entity.{InputURL, ShortURL, ValidURL}

import scala.concurrent.ExecutionContext

trait UrlShortenerService {
  def validate(url: String): Boolean
  def shorten(url: ValidURL): ShortURL
  def get(code: String): InputURL
}

class UrlShortenerServiceImpl(implicit val executionContext: ExecutionContext) extends UrlShortenerService {

  val urlMap = collection.mutable.Map[String, String]()
  final val alphabet: String = "bcdfghjkmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ23456789-_"
  final val randomStringLength = 8
  final val urlPattern = "([A-Za-z]{3,9}:(?:\\/\\/)?)+([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*" +
    "((?!(10\\.|172\\.(1[6-9]|2\\d|3[01])\\.|192\\.168\\.).*)(?!255\\.255\\.255\\.255)" +
    "(25[0-5]|2[1,3]\\d|[1]\\d\\d|[1-9]\\d|[1-9])(\\.(25[0-5]|2[0-4]\\d|[1]\\d\\d|[1-9]\\d|\\d)){3}|" +
    "[^\\.\\-\\s]([a-zA-Z0-9-]+\\.)*[a-zA-Z0-9-]+\\." +
    "(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{1,9})|" +
    "(\\[(([\\da-fA-F]{4}(:|::)){1}([\\da-fA-F]{1,4}(:|::)){1,6}([:]{1,2})?" +
    "([\\da-fA-F]{1,4}){1})\\])|((?:\\+?(\\d{1}))?[- (](\\d{3})[- )](\\d{3})[- ](\\d{1,4})))(:[0-9]+)*" +
    "(\\/($|[a-zA-Z0-9\\.,\\?'\\\\\\+&%$#=~_\\-\\(\\)]+)?)*"

  override def validate(url: String): Boolean = Pattern.matches(urlPattern, url)

  private def save(shortName: String, input: String): ShortURL = {
    urlMap += shortName -> input
    println(urlMap.toString)
    ShortURL(shortName)
  }

  override def shorten(url: ValidURL): ShortURL = {
    if (urlMap.exists(_._2 == url.url)) {
      val short = urlMap.filter { case (k, v) => v == url.url }.keys.mkString
      ShortURL(short)
    } else {
      val sb = new StringBuilder
      for (i <- 1 to randomStringLength) {
        val randomNum = util.Random.nextInt(alphabet.length)
        sb.append(alphabet(randomNum))
      }
      val shortName = "www.short.com/" + sb.toString
      save(shortName, url.url)
    }
  }

  override def get(url: String): InputURL = {
    InputURL(urlMap(url))
  }
}
