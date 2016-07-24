lazy val coffee = project
  .copy(id = "coffee")
  .in(file("."))
  .enablePlugins(AutomateHeaderPlugin, GitVersioning)

name := "coffee"

libraryDependencies ++= Vector(
  Library.akkaHttp,
  Library.akkaLog4j,
  Library.akkaPersistenceQuery,
  Library.akkaPersistenceCassandra,
  Library.akkaCamel,
  Library.camelRabbitMQ,
  Library.akkaHttpCirce,
  Library.circeGeneric,
  Library.circeParser,
  Library.circeJava8,
  Library.log4jCore,
  Library.slf4jLog4jBridge,
  Library.swaggerAkka,
  Library.scalaCheck           % "test",
  Library.scalaTest            % "test",
  Library.akkaTestkit          % "test",
  Library.akkaHttpTestkit      % "test"
)

initialCommands := """|import nl.codecentric.coffee._
                      |""".stripMargin
