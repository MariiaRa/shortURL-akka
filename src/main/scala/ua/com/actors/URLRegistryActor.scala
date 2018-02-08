package ua.com.actors
import akka.actor._
import ua.com.entity._

object URLRegistryActor{
  def props(makeShortURLActor: ActorRef): Props = Props(new URLRegistryActor(makeShortURLActor))
  val urlMap = collection.mutable.Map[String, String]()
  case class NotFoundMessage(str: ValidURL)

  private def findKeyByValue(map: collection.mutable.Map[String, String], value: String): String = {
    map.filter{ case (k, v) => v == value }.keys.mkString
  }
}

class URLRegistryActor(makeShortURLActor: ActorRef) extends Actor {

  import URLRegistryActor._

    override def receive: Receive = {
    case x: ValidURL =>
      println("Searching db...")
      if (urlMap.exists(_._2 == x.url)) {
        println("found!")
        val short = findKeyByValue(urlMap, x.url)
        sender() ! ShortURL(short)
       } else {
        println("No such url in db")
        sender() ! NotFoundMessage(x)
            }
    case a: (ShortURL, ValidURL)  =>
      println("Saved short url:" + a._1.url)
      urlMap += a._1.url -> a._2.url
      println("Map:" + urlMap.toString)
        sender() ! a._1
  }
}
