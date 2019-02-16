/*
 * (C) Copyright 2019 zvreifnitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

logLevel := sbt.util.Level.Info

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
  .aggregate(javaUtils, scalaUtils, javaDi, javaDiGuice, javaDeps)

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

lazy val javaDi = (project in file("java-di"))
  .withId("java-di")
  .dependsOn(javaUtils)
  .settings(
    javaSettings,
    name := "java-di",
    libraryDependencies += "javax.inject" % "javax.inject" % "1"
  )

lazy val javaDiGuice = (project in file("java-di-guice"))
  .withId("java-di-guice")
  .dependsOn(javaDi)
  .settings(
    javaSettings,
    name := "java-di-guice",
    libraryDependencies += "com.google.inject" % "guice" % "4.2.2"
  )

lazy val javaDeps = (project in file("java-deps"))
  .withId("java-deps")
  .dependsOn(javaUtils)
  .settings(
    javaSettings,
    name := "java-deps"
  )