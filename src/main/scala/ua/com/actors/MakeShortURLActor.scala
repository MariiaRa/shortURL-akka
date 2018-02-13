import akka.actor._
import ua.com.actors.CoordinatorActor.SaveURL
import ua.com.entity.ValidURL

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
    case a: ValidURL =>
      println("Received valid url for shortening")
      val shortName = "www."+ randomAlphaNumericString(alphabet)
     /* println("Short url: " + shortName)*/
   //  sender() ! (ShortURL(shortName), a)
      sender() ! SaveURL(shortName, a.url)
  }
}