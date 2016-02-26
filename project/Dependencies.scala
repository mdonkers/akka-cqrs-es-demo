import sbt._

object Version {
  final val Akka = "2.4.2"
  final val AkkaPersistenceCassandra = "0.10"
  final val AkkaLog4j = "1.1.2"
  final val AkkaHttpJson = "1.5.2"
  final val Circe = "0.3.0"
  final val Log4j = "2.5"
  final val SwaggerAkka = "0.6.2"
  final val Scala = "2.11.7"
  final val ScalaCheck = "1.13.0"
  final val ScalaTest = "2.2.6"
}

object Library {
  val akkaHttp = "com.typesafe.akka" %% "akka-http-experimental" % Version.Akka
  val akkaPersistence = "com.typesafe.akka" %% "akka-persistence" % Version.Akka
  val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % Version.AkkaHttpJson
  val circeGeneric = "io.circe" %% "circe-generic" % Version.Circe
  val circeJava8 = "io.circe" %% "circe-java8" % Version.Circe
  val akkaPersistenceCassandra = "com.typesafe.akka" %% "akka-persistence-cassandra" % Version.AkkaPersistenceCassandra
  val akkaLog4j = "de.heikoseeberger" %% "akka-log4j" % Version.AkkaLog4j
  val log4jCore = "org.apache.logging.log4j" % "log4j-core" % Version.Log4j
  val slf4jLog4jBridge = "org.apache.logging.log4j" % "log4j-slf4j-impl" % Version.Log4j
  val swaggerAkka = "com.github.swagger-akka-http" %% "swagger-akka-http" % Version.SwaggerAkka
  val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.ScalaCheck
  val scalaTest = "org.scalatest" %% "scalatest" % Version.ScalaTest
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % Version.Akka
  val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % Version.Akka
}
