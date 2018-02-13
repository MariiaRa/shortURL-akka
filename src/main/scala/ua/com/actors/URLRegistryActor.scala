package ua.com.actors
import akka.actor._
import ua.com.actors.CoordinatorActor.SaveURL
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
    case a: SaveURL  =>
      println("Saved short url:" + a.url1)
      urlMap += a.url1 -> a.url2
      println("Map:" + urlMap.toString)
        sender() ! ShortURL(a.url1)
  }
}
