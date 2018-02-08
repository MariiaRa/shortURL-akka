import akka.actor._
import ua.com.entity.{ShortURL, ValidURL}

object MakeShortURLActor {
  def props: Props = Props[MakeShortURLActor]

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
}

class MakeShortURLActor extends Actor {
  import MakeShortURLActor._

  override def receive = {
    case ValidURL(url) =>
      println("Received valid url for shortening")
      val shortName = "www.short-url/"+ randomAlphaNumericString(alphabet)
     /* println("Short url: " + shortName)*/
     sender() ! (ShortURL(shortName), ValidURL(url))
  }
}