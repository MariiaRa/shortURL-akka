package ua.com

import java.sql.{Connection, DriverManager}

object DBConfig {
  private val DB_DRIVER: String = "org.h2.Driver"
  private val DB_CONNECTION: String = "jdbc:h2:~/urlShortener3"
  private val DB_USER: String = "sa"
  private val DB_PASSWORD: String = ""
  Class.forName(DB_DRIVER) //To connect to a database, an application first needs to load the database driver
  val dbConnection: Connection = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD) //and then get a connection
}
