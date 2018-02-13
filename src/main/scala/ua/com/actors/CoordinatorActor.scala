package ua.com.actors


import akka.actor._
import ua.com.actors.URLRegistryActor.NotFoundMessage
import ua.com.entity._

object CoordinatorActor{
  def props(validator: ActorRef, urlRegistryActor: ActorRef, makeShortURLActor: ActorRef): Props =
    Props(new CoordinatorActor(validator, urlRegistryActor, makeShortURLActor))

  case class SaveURL(url1: String, url2: String)

}

class CoordinatorActor(validator: ActorRef, urlRegistryActor: ActorRef, makeShortURLActor: ActorRef) extends Actor{
  import CoordinatorActor._


   override def receive = {
       case a: InputURL =>
         println("Received input url")
         validator ! a
       case b: ValidURL =>
         urlRegistryActor ! b
       case c: NotFoundMessage =>
         println("Received valid url for saving")
         makeShortURLActor ! c.str
       case d: SaveURL =>
         urlRegistryActor ! d
       case e: ShortURL =>
println(e.url)
                  }




   }

