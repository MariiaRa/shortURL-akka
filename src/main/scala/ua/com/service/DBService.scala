package ua.com.service

import java.sql.ResultSet

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import ua.com.entity._

class DBService(urlService: UrlService) {
  import ua.com.DBConfig.dbConnection

  private val logger = Logger(LoggerFactory.getLogger("DB service logger"))
  val urlPrefix: String = "www.short.com/"

  def fetchByLongName (url: ValidURL): ShortURL = {
    val stmt = dbConnection.createStatement()
    try {
      val query: String = "SELECT SHORT_URL FROM urls WHERE LONG_URL = '" + url.url + "'"
      val rs: ResultSet = stmt.executeQuery(query)

      if (rs.next()) {
        logger.info(s"The requested URL(${url.url}) is already shortened and saved in DB.")
        val shortName: String = urlPrefix + rs.getString("SHORT_URL")
        ShortURL(shortName)
      } else {
        val shortName: String = urlService.makeShort(url)
        saveDB(shortName, url.url)
      }
    } finally {
      stmt.close()
    }
  }

  def saveDB(short: String, long: String): ShortURL = {
    val stmt = dbConnection.createStatement()
    try {
      val query: String = "INSERT INTO urls ( SHORT_URL, LONG_URL, CLICKS ) VALUES('" + short + "', '" + long + "', 0)"
      stmt.execute(query)
      val shortName: String = urlPrefix + short
      logger.debug(s"The requested URL $long is saved to DB as $short.")
      ShortURL(shortName)
    } finally {
      stmt.close()
    }
  }

  def fetchByShortName(shortURL: String): Option[InputURL] = {
    val shortName: String = shortURL.split("/")(1)
    val stmt = dbConnection.createStatement()
    try {
      val query: String = "SELECT LONG_URL FROM urls WHERE SHORT_URL = '" + shortName + "'"
      val rs: ResultSet = stmt.executeQuery(query)
      logger.debug(s"Searching for short url $shortURL in DB")
      if (rs.next()) {
        val longURL: String = rs.getString("LONG_URL")
        stmt.executeUpdate("UPDATE urls SET CLICKS = CLICKS+1" + "WHERE SHORT_URL = '" + shortName + "'")
        Some(InputURL(longURL))
      } else {
        logger.error(s"An error occurred while getting URL $shortName, couldn't find this short url in DB")
        None
      }
    } finally {
      stmt.close()
    }
  }

  def getAllStats:Option[AllStats] = {
    val stmt = dbConnection.createStatement()
    try {
      val rs: ResultSet = stmt.executeQuery("SELECT COUNT(*) AS total_count, SUM(CLICKS) AS total_clicks FROM urls")
      logger.debug("Fetching url total stats")
      if (rs.next()) {
        val totalCount = rs.getInt("total_count")
        val totalClicks = rs.getInt("total_clicks")
        Some(AllStats(totalCount, totalClicks))
      } else {
        logger.error(s"An error occurred while requesting url total stats from DB")
        None
      }
    } finally {
      stmt.close()
    }
  }

  def getURLStats (shortURL: ShortURL):Option[URLStats] = {
    val shortName: String = shortURL.url.split("/")(1)
    val stmt = dbConnection.createStatement()
    try {
      val rs: ResultSet = stmt.executeQuery("SELECT CLICKS AS url_clicks FROM urls WHERE SHORT_URL = '" + shortName+"'")
      logger.debug(s"Fetching stats for url ${shortURL.url}")
      if (rs.next()) {
        val singleURLClicks = rs.getInt("url_clicks")
        Some(URLStats(singleURLClicks))
      } else {
        logger.error(s"An error occurred while requesting stats for url ${shortURL.url}, couldn't find this short url in DB")
        None
      }
    } finally {
      stmt.close()
    }
  }
}
