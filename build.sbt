name := "URLShortener-akka"
name := "URLShortener"
organization := "software.sigma"
version := "0.1"
scalaVersion := "2.12.4"

//akka actors
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.9",
  "com.typesafe.akka" %% "akka-http" % "10.0.11",
  "com.typesafe.akka" %% "akka-stream" % "2.5.9",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.0-RC1",

  "com.typesafe.akka" %% "akka-testkit" % "2.5.9" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.11" % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.9" % Test
)
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.4"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"
//testing
libraryDependencies += "org.specs2" %% "specs2-core" % "4.0.2" % "test"
libraryDependencies += "joda-time" % "joda-time" % "2.9.9"

//resolvers are alternate resources provided for the projects on which there is a dependency
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

scalacOptions in Test ++= Seq("-Yrangepos")