import sbt._

object Dependencies {
  val JUnit = Seq(
    "org.junit.jupiter" % "junit-jupiter" % "5.4.0" % Test,
    "org.junit.jupiter" % "junit-jupiter-api" % "5.4.0" % Test,
    "org.junit.jupiter" % "junit-jupiter-engine" % "5.4.0" % Test,
    "org.junit.jupiter" % "junit-jupiter-migrationsupport" % "5.4.0" % Test,
    "org.junit.jupiter" % "junit-jupiter-params" % "5.4.0" % Test,
    "org.junit.platform" % "junit-platform-commons" % "1.4.0" % Test,
    "org.junit.platform" % "junit-platform-console" % "1.4.0" % Test,
    "org.junit.platform" % "junit-platform-engine" % "1.4.0" % Test,
    "org.junit.platform" % "junit-platform-launcher" % "1.4.0" % Test,
    "org.junit.platform" % "junit-platform-reporting" % "1.4.0" % Test,
    "org.junit.platform" % "junit-platform-runner" % "1.4.0" % Test,
    "org.junit.platform" % "junit-platform-suite-api" % "1.4.0" % Test,
    "org.junit.platform" % "junit-platform-testkit" % "1.4.0" % Test,
    "org.junit.vintage" % "junit-vintage-engine" % "5.4.0" % Test
  )

  val JavaInject = "javax.inject" % "javax.inject" % "1"

  val Guice = "com.google.inject" % "guice" % "4.2.2"
}
