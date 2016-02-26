lazy val coffee = project
  .copy(id = "coffee")
  .in(file("."))
  .enablePlugins(AutomateHeaderPlugin, GitVersioning)

name := "coffee"

libraryDependencies ++= Vector(
  Library.akkaHttp,
  Library.akkaLog4j,
  Library.akkaPersistenceCassandra,
  Library.circeGeneric,
  Library.circeJava8,
  Library.log4jCore,
  Library.slf4jLog4jBridge,
  Library.akkaHttpCirce,
  Library.swaggerAkka,
  Library.scalaCheck % "test",
  Library.scalaTest % "test",
  Library.akkaTestkit          % "test",
  Library.akkaHttpTestkit      % "test"
)

initialCommands := """|import nl.codecentric.coffee._
                      |""".stripMargin
