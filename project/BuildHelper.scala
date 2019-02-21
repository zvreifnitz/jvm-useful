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
          if (f.exists()) f else baseDirectory.value
        },
        target := targetDir,
        name := projName
      )
    res
  }
}
