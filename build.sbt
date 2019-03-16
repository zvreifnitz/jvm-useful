import BuildHelper._
import CustomKeys._
import Dependencies._

logLevel in Global := sbt.util.Level.Info
publishMavenStyle in Global := true
organization in Global := "com.github.zvreifnitz"
scalaVersion in Global := "2.12.8"
version in Global := "1.0.0-SNAPSHOT"
compileOrder in Global := sbt.CompileOrder.JavaThenScala
rootSrcDir in Global := "."
organizationName in Global := "zvreifnitz"
startYear in Global := Some(2019)
licenses in Global += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))

lazy val projCommonSettings = Seq(
  test in assembly := {}
)

lazy val projScalaSettings = projCommonSettings ++ Seq(
  crossPaths := true,
  autoScalaLibrary := true
)

lazy val projJavaSettings = projCommonSettings ++ Seq(
  crossPaths := false,
  autoScalaLibrary := false,
  libraryDependencies ++= JUnit
)

lazy val all = makeProject("all")
  .aggregate(javaUtils, scalaUtils, javaDeps, javaSettings, javaDi, javaMsg, javaDiGuice)

lazy val javaUtils = makeProject("java-utils")
  .enablePlugins(JavaAppPackaging)
  .settings(projJavaSettings)

lazy val scalaUtils = makeProject("scala-utils")
  .enablePlugins(JavaAppPackaging)
  .dependsOn(javaUtils)
  .settings(projScalaSettings)

lazy val javaDeps = makeProject("java-deps")
  .enablePlugins(JavaAppPackaging)
  .dependsOn(javaUtils)
  .settings(projJavaSettings)

lazy val javaSettings = makeProject("java-settings")
  .enablePlugins(JavaAppPackaging)
  .dependsOn(javaUtils, javaDeps)
  .settings(projJavaSettings)

lazy val javaDi = makeProject("java-di")
  .enablePlugins(JavaAppPackaging)
  .dependsOn(javaUtils)
  .settings(
    projJavaSettings,
    libraryDependencies += JavaInject
  )

lazy val javaMsg = makeProject("java-msg")
  .enablePlugins(JavaAppPackaging)
  .dependsOn(javaUtils, javaDeps)
  .settings(
    projJavaSettings
  )

lazy val javaDiGuice = makeProject("java-di-guice")
  .enablePlugins(JavaAppPackaging)
  .dependsOn(javaDi)
  .settings(
    projJavaSettings,
    libraryDependencies += Guice
  )