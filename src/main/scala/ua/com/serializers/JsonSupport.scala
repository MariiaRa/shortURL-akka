package ua.com.serializers

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol._
import ua.com.entity.{InputURL, ShortURL}

trait JsonSupport extends SprayJsonSupport {

  implicit val inputURLJsonFormat = jsonFormat1(InputURL)
  implicit val shortURLJsonFormat = jsonFormat1(ShortURL)

}