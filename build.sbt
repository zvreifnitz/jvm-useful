import BuildHelper._
import CustomKeys._

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

lazy val commonSettings = Seq(
  test in assembly := {}
)

lazy val scalaSettings = commonSettings ++ Seq(
  crossPaths := true,
  autoScalaLibrary := true
)

lazy val javaSettings = commonSettings ++ Seq(
  crossPaths := false,
  autoScalaLibrary := false
)

lazy val all = makeProject("all")
  .aggregate(javaUtils, scalaUtils, javaDi, javaDiGuice, javaDeps)

lazy val javaUtils = makeProject("java-utils")
  .enablePlugins(JavaAppPackaging)
  .settings(javaSettings)

lazy val scalaUtils = makeProject("scala-utils")
  .enablePlugins(JavaAppPackaging)
  .dependsOn(javaUtils)
  .settings(scalaSettings)

lazy val javaDi = makeProject("java-di")
  .enablePlugins(JavaAppPackaging)
  .dependsOn(javaUtils)
  .settings(
    javaSettings,
    libraryDependencies += "javax.inject" % "javax.inject" % "1"
  )

lazy val javaDiGuice = makeProject("java-di-guice")
  .enablePlugins(JavaAppPackaging)
  .dependsOn(javaDi)
  .settings(
    javaSettings,
    libraryDependencies += "com.google.inject" % "guice" % "4.2.2"
  )

lazy val javaDeps = makeProject("java-deps")
  .enablePlugins(JavaAppPackaging)
  .dependsOn(javaUtils)
  .settings(javaSettings)