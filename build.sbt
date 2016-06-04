enablePlugins(JavaAppPackaging)

name := """storyline"""
organization := "org.dohrm"
version := "1.0"
scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.4.3"
  val scalaTestV = "2.2.6"
  val slickV = "3.1.1"
  val slickJodaMapperVersion = "2.2.0"
  val postgressV = "9.4-1206-jdbc42"
  val h2V = "1.4.191"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
    "com.github.tototoshi" %% "slick-joda-mapper" % slickJodaMapperVersion,
    "com.auth0" % "java-jwt" % "2.1.0",
    "commons-codec" % "commons-codec" % "1.4",
    "joda-time" % "joda-time" % "2.7",
    "org.joda" % "joda-convert" % "1.7",
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "com.typesafe.slick" %% "slick" % slickV,
    "org.postgresql" % "postgresql" % postgressV,
    "com.h2database" % "h2" % h2V,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaV % "test",
    "org.scalatest" %% "scalatest" % scalaTestV % "test"
  )
}

Revolver.settings
