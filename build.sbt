
lazy val commonSettings = Seq(
  publishMavenStyle := true,
  organization := "com.github.zvreifnitz",
  scalaVersion := "2.12.8",
  version := "1.0.0-SNAPSHOT",
  test in assembly := {}
)

lazy val scalaSettings = commonSettings ++ Seq(
  crossPaths := true,
  autoScalaLibrary := true,
  compileOrder := sbt.CompileOrder.JavaThenScala
)

lazy val javaSettings = commonSettings ++ Seq(
  crossPaths := false,
  autoScalaLibrary := false,
  compileOrder := sbt.CompileOrder.ScalaThenJava
)

lazy val root = (project in file("."))
  .aggregate(javaUtils, scalaUtils, javaConcurrency, scalaConcurrency)

lazy val javaUtils = (project in file("java-utils"))
  .withId("java-utils")
  .settings(
    javaSettings,
    name := "java-utils"
  )

lazy val scalaUtils = (project in file("scala-utils"))
  .withId("scala-utils")
  .dependsOn(javaUtils)
  .settings(
    scalaSettings,
    name := "scala-utils"
  )

lazy val javaConcurrency = (project in file("java-concurrency"))
  .withId("java-concurrency")
  .dependsOn(javaUtils)
  .settings(
    javaSettings,
    name := "java-concurrency"
  )

lazy val scalaConcurrency = (project in file("scala-concurrency"))
  .withId("scala-concurrency")
  .dependsOn(javaUtils, scalaUtils, javaConcurrency)
  .settings(
    scalaSettings,
    name := "scala-concurrency"
  )