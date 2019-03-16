import java.nio.file.Paths

import CustomKeys.rootSrcDir
import sbt.Keys._
import sbt.{Project, file, project}

object BuildHelper {

  def makeProject(projDir: String): Project = {
    val projName = projDir.split("/").last
    val targetDir = file("." + "/target/" + projName).getCanonicalFile
    val res = (project in file(projDir))
      .withId(projName)
      .settings(
        baseDirectory := {
          val f = file(rootSrcDir.value + "/" + projDir).getCanonicalFile
          val baseDir = if (f.exists()) f else baseDirectory.value
          if (projDir != null && projDir.startsWith("java")) {
            makeJavaDirs(baseDir.getAbsolutePath)
          } else if (projDir != null && projDir.startsWith("scala")) {
            makeScalaDirs(baseDir.getAbsolutePath)
          }
          baseDir
        },
        target := targetDir,
        name := projName
      )
    res
  }

  private[this] def makeJavaDirs(basePath: String): Unit = {
    Paths.get(basePath, "src", "main", "java").toFile.mkdirs()
    Paths.get(basePath, "src", "test", "java").toFile.mkdirs()
    Paths.get(basePath, "src", "main", "resources").toFile.mkdirs()
    Paths.get(basePath, "src", "test", "resources").toFile.mkdirs()
  }

  private[this] def makeScalaDirs(basePath: String): Unit = {
    Paths.get(basePath, "src", "main", "scala").toFile.mkdirs()
    Paths.get(basePath, "src", "test", "scala").toFile.mkdirs()
    Paths.get(basePath, "src", "main", "resources").toFile.mkdirs()
    Paths.get(basePath, "src", "test", "resources").toFile.mkdirs()
  }
}
