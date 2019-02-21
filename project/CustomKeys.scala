import sbt.settingKey

object CustomKeys {
  lazy val rootSrcDir = settingKey[String]("Provides root src directory")
}
