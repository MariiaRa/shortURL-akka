package ua.com.Service

import java.sql.ResultSet
import java.util.regex.Pattern

import ua.com.entity._

import scala.concurrent.ExecutionContext

/*trait UrlShortenerService {
  def validate(url: String): Boolean
  def shorten(url: ValidURL): ShortURL
  def get(code: String): InputURL
}*/

class UrlService(implicit val executionContext: ExecutionContext) {

  import ua.com.DBConfig.dbConnection

  //val urlMap = collection.mutable.Map[String, String]()
  val alphabet: String = "bcdfghjkmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ23456789-_"
  val randomStringLength = 8
  val urlPattern = "([A-Za-z]{3,9}:(?:\\/\\/)?)+([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*" +
    "((?!(10\\.|172\\.(1[6-9]|2\\d|3[01])\\.|192\\.168\\.).*)(?!255\\.255\\.255\\.255)" +
    "(25[0-5]|2[1,3]\\d|[1]\\d\\d|[1-9]\\d|[1-9])(\\.(25[0-5]|2[0-4]\\d|[1]\\d\\d|[1-9]\\d|\\d)){3}|" +
    "[^\\.\\-\\s]([a-zA-Z0-9-]+\\.)*[a-zA-Z0-9-]+\\." +
    "(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{1,9})|" +
    "(\\[(([\\da-fA-F]{4}(:|::)){1}([\\da-fA-F]{1,4}(:|::)){1,6}([:]{1,2})?" +
    "([\\da-fA-F]{1,4}){1})\\])|((?:\\+?(\\d{1}))?[- (](\\d{3})[- )](\\d{3})[- ](\\d{1,4})))(:[0-9]+)*" +
    "(\\/($|[a-zA-Z0-9\\.,\\?'\\\\\\+&%$#=~_\\-\\(\\)]+)?)*"

  def validate(url: String): Boolean = Pattern.matches(urlPattern, url)


  def fetchByLong (url: ValidURL): ShortURL = {
    val stmt = dbConnection.createStatement()
    try {
      val select: String = "SELECT SHORT_URL FROM urls WHERE LONG_URL = '" + url.url + "'"
      val rs: ResultSet = stmt.executeQuery("SELECT SHORT_URL FROM urls WHERE LONG_URL = '" + url.url + "'")

      if (rs.next()) {
        val short: String = rs.getString("SHORT_URL")
        ShortURL(short)
      } else {
        val short = shorten2
        saveDB(short, url.url)
      }
    } finally {
      stmt.close()
    }
  }

  def saveDB(short: String, long: String): ShortURL = {
    val stmt = dbConnection.createStatement()
    try {
      val query: String = "INSERT INTO urls ( SHORT_URL, LONG_URL, CLICKS ) VALUES('" + short + "', '" + long + "', 0)"
      println(query)
      stmt.execute(query)
      println("saved")
      ShortURL(short)
    }finally {
      stmt.close()
    }
  }

 /* private def save(shortName: String, input: String): ShortURL = {
    urlMap += shortName -> input
    println(urlMap.toString)
    ShortURL(shortName)
  }*/

 /* override def shorten(url: ValidURL): ShortURL = {
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
  }*/

  def shorten2: String = {
        val sb = new StringBuilder
      for (i <- 1 to randomStringLength) {
        val randomNum = util.Random.nextInt(alphabet.length)
        sb.append(alphabet(randomNum))
      }
      val shortName = "www.short.com/" + sb.toString
    shortName
    }

 /* override def get(url: String): InputURL = {
    InputURL(urlMap(url))
  }*/

  def fetchByShort(short: String): Option[InputURL] = {
    val stmt = dbConnection.createStatement()
    try {
      val select: String = "SELECT LONG_URL FROM urls WHERE SHORT_URL = '" + short + "'"
      println(select)
      val rs: ResultSet = stmt.executeQuery("SELECT LONG_URL, CLICKS FROM urls WHERE SHORT_URL = '" + short + "'")
      if (rs.next()) {
        val long: String = rs.getString("LONG_URL")
        stmt.executeUpdate("UPDATE urls SET CLICKS = CLICKS+" + 1 + "WHERE SHORT_URL = '" + short + "'")
        println("long: " + long)
        Some(InputURL(long))
      } else {
        None
      }
    }finally {
      stmt.close()
    }
  }

  def getAllStats:Option[AllStats] = {
    //case class StatsURL(totalURLCount: Int, totalClickCount: Int, singleURLCount)
    val stmt = dbConnection.createStatement()
    //val totalURL
    //SELECT COUNT(*) FROM table
    try {
      val rs: ResultSet = stmt.executeQuery("SELECT COUNT(*) AS total_count, SUM(CLICKS) AS total_clicks FROM urls")
      //val totalURL = rs1.getInt("total_count")
      if (rs.next()) {
        val totalURL = rs.getInt("total_count")
        val totalClicks = rs.getInt("total_clicks")
        println(s"stats: $totalURL, $totalClicks")
        Some(AllStats(totalURL, totalClicks))
      } else {
        None
      }
    }finally {
      stmt.close()
    }
  }

 def getURLStats (url: ShortURL):Option[URLStats] = {
   val stmt = dbConnection.createStatement()
   try {
     val rs: ResultSet = stmt.executeQuery("SELECT CLICKS AS url_clicks FROM urls WHERE SHORT_URL = '" + url.url+"'")
     if (rs.next()) {
       val singleURLClicks = rs.getInt("url_clicks")
       Some(URLStats(singleURLClicks))
     } else {
       None
     }
  }finally {
     stmt.close()
   }
 }

}
