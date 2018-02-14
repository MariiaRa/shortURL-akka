package ua.com.entity

case class AllStats(totalURLCount: Int, totalClickCount: Int)
case class URLStats(clickCount: Int)
case class InputURL (url: String)
case class ShortURL (url: String)
case class ValidURL (url: String)
