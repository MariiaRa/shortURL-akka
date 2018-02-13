package ua.com.service
import java.sql.{Connection, DriverManager, ResultSet, SQLException}

import ua.com.entity.{ShortURL, ValidURL}

object DBService {

  final val DB_DRIVER: String = "org.h2.Driver"
  final val DB_CONNECTION: String = "jdbc:h2:~/urlShortener"
  final val DB_USER: String = "sa"
  final val DB_PASSWORD: String = ""

  private def getDBConnection(): Connection = {
    try {
      Class.forName(DB_DRIVER)
    } catch {
      case ex: ClassNotFoundException => println(ex.getMessage)
    }
    val dbConnection: Connection = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD)
    dbConnection
  }

  private def createDbStructure(conn: Connection): Unit = {
    val sql1 = """
      create schema if not exists urlShortener;

      set schema urlShortener;

      create table if not exists urls (
        id int auto_increment primary key,
        short_url varchar(255) not null,
        long_url varchar(255) not null,
       clicks int not null);"""

    val sql2: String = "create INDEX ind_short on urls(short_url);"
    val stmt = conn.createStatement()
    try {
      stmt.execute(sql1)
      stmt.execute(sql2)
      println("DB created")
    } finally {
      stmt.close
    }
  }

  def main(args: Array[String]): Unit = {

    val conn: Connection = getDBConnection()
    createDbStructure(conn)
    val stmt = conn.createStatement()
    try {
      // val stmt = conn.createStatement()
      //stmt.executeUpdate( "DROP TABLE table1" );

      // stmt.executeUpdate( "INSERT INTO urls ( SHORT_URL, LONG_URL, CLICKS ) VALUES ( 'shortUrl', 'longUrl', '0')" )

      // println("ok")

      def saveDB(short: String, long: String): ShortURL = {
        //check if url exists in db !!!
        val query: String = "INSERT INTO urls ( SHORT_URL, LONG_URL ) VALUES('" + short + "', '" + long + "')"
        println(query)
        stmt.execute(query)
        println("saved")
        ShortURL(short)
      }

      def fetchByShort(short: String): Unit = {
        val select: String = "SELECT LONG_URL FROM urls WHERE SHORT_URL = '" + short + "'"
        println(select)
        val rs: ResultSet = stmt.executeQuery("SELECT LONG_URL, CLICKS FROM urls WHERE SHORT_URL = '" + short + "'")
        while (rs.next()) {
          val long: String = rs.getString("LONG_URL")
          println("long: " + long)
          //InputURL(long)
        }
        stmt.executeUpdate("UPDATE urls SET CLICKS = CLICKS+" + 1 + "WHERE SHORT_URL = '" + short + "'")
      }

      def fetchByLong (url: ValidURL): Option[ShortURL] = {
        val select: String = "SELECT SHORT_URL FROM urls WHERE LONG_URL = '" + url.url + "'"
        val rs: ResultSet = stmt.executeQuery("SELECT SHORT_URL FROM urls WHERE LONG_URL = '" + url.url + "'")

        if (rs.next()) {
          val short: String = rs.getString("SHORT_URL")
          Some(ShortURL(short))
        } else {
          None
        }
      }

      def urlStats(short: String) = {
        //case class StatsURL(totalURLCount: Int, totalClickCount: Int)

        //val totalURL
        //SELECT COUNT(*) FROM table
        val rs1: ResultSet = stmt.executeQuery("SELECT COUNT(*) AS total_count FROM urls")
        //val totalURL = rs1.getInt("total_count")
        while (rs1.next()) {
          println(rs1.getInt("total_count"))
        }

        //all requests
        // SELECT SUM(CLICKS)FROM urls
        val rs2: ResultSet = stmt.executeQuery("SELECT SUM(CLICKS) AS total_clicks FROM urls")
        // val totalClicks = rs1.getInt("total_clicks")

        println(rs2)
        //clicks per url
        //"SELECT CLICKS FROM urls WHERE SHORT_URL = '" + short+"'"
        // val rs3:ResultSet=stmt.executeQuery("SELECT CLICKS AS url_clicks FROM urls WHERE SHORT_URL = '" + short+"'")
        //  val singleURLClicks = rs3.getInt("url_clicks")
        //  println(singleURLClicks)
      }

      //saveDB(args(0), args(1))
      // fetchByShort(args(0))
      //  urlStats(args(0))

    } finally {
      // stmt.close()
      conn.close()
    }
  }
}
