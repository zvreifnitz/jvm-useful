import $file.deps
import $file.jmh
import jmh.JmhModule
import mill._
import mill.define.Target
import mill.moduledefs.Cacher
import mill.scalalib._

trait GenDirs {
  outer: JavaModule with Cacher =>

  def moduleType: String

  def genDirs() = T.command {
    genDirsFromSources(sources(), os.RelPath(rootPckg))
    genDirsFromSources(resources(), os.RelPath.rel)
  }

  final def rootPckg = "com/github/zvreifnitz/" + moduleName.toLowerCase

  def moduleName: String = {
    val fullname = this.getClass.getSimpleName
    val cleanname = fullname.replaceAll("[^a-zA-Z0-9_]", "")
    val len = math.min(fullname.length, cleanname.length)
    var result = ""
    (1 to len).foreach(l => {
      val s1 = fullname.substring(0, l)
      val s2 = cleanname.substring(0, l)
      if (s1 == s2) {
        result = s1
      }
    })
    result.replaceAll("_" + moduleType, "")
  }

  private def genDirsFromSources(sources: Seq[PathRef], rootPckg: os.RelPath): Unit = {
    for {
      pathRef <- sources if !os.exists(pathRef.path / rootPckg)
    } os.makeDir.all(pathRef.path / rootPckg)
  }
}

trait JModule extends JavaModule with GenDirs {
  outer =>

  final override def moduleType = "java"

  final override def moduleName: String = super.moduleName

  override def forkArgs = T {
    Seq("-XX:-RestrictContended", "-XX:+PreserveFramePointer")
  }

  override def javacOptions = T {
    Seq("--add-exports", "java.base/jdk.internal.vm.annotation=ALL-UNNAMED")
  }

  override def sources = T.sources(
    millSourcePath / 'src / 'main / 'java
  )

  override def resources = T.sources {
    millSourcePath / 'src / 'main / 'resources
  }

  override def intellijModulePath = millSourcePath

  trait Examples extends JavaModule with GenDirs {

    final override def moduleType = outer.moduleType

    final override def moduleName = outer.moduleName

    override def moduleDeps: Seq[JavaModule] = outer.moduleDeps ++ Seq(outer)

    override def ivyDeps: Target[Agg[Dep]] = T {
      super.ivyDeps()
    }

    override def sources = T.sources(
      millSourcePath / 'src / 'examples / 'java
    )

    override def resources = T.sources {
      millSourcePath / 'src / 'examples / 'resources
    }

    override def millSourcePath = outer.millSourcePath
  }

  trait Tests extends JavaModuleTests with GenDirs {

    final override def moduleType = outer.moduleType

    final override def moduleName = outer.moduleName

    override def testFrameworks = Seq("com.novocode.junit.JUnitFramework")

    override def ivyDeps: Target[Agg[Dep]] = T {
      Agg(ivy"com.novocode:junit-interface:0.11")
    }

    override def sources = T.sources(
      millSourcePath / 'src / 'test / 'java
    )

    override def millSourcePath = outer.millSourcePath

    override def resources = T.sources {
      millSourcePath / 'src / 'test / 'resources
    }
  }

  trait Benchmarks extends JmhModule with GenDirs {

    final override def moduleType = outer.moduleType

    final override def moduleName = outer.moduleName

    override def moduleDeps: Seq[JavaModule] = Seq(outer)

    override def ivyDeps: Target[Agg[Dep]] = T {
      super.ivyDeps()
    }

    override def sources = T.sources(
      millSourcePath / 'src / 'test_perf / 'java
    )

    override def resources = T.sources {
      millSourcePath / 'src / 'test_perf / 'resources
    }

    override def millSourcePath = outer.millSourcePath
  }

}

trait SModule extends ScalaModule with GenDirs {
  outer =>

  final override def moduleType = "scala"

  final override def moduleName: String = super.moduleName

  def scalaVersion = "2.13.5"

  override def forkArgs = T {
    Seq("-XX:-RestrictContended", "-XX:+PreserveFramePointer")
  }

  override def sources = T.sources(
    millSourcePath / 'src / 'main / 'scala
  )

  override def resources = T.sources {
    millSourcePath / 'src / 'main / 'resources
  }

  override def intellijModulePath = millSourcePath

  trait Tests extends ScalaModuleTests with GenDirs {

    final override def moduleType = outer.moduleType

    final override def moduleName = outer.moduleName

    override def testFrameworks = Seq("com.novocode.junit.JUnitFramework")

    override def ivyDeps = T {
      Agg(ivy"com.novocode:junit-interface:0.11")
    }

    override def sources = T.sources(
      millSourcePath / 'src / 'test / 'scala
    )

    override def millSourcePath = outer.millSourcePath

    override def resources = T.sources {
      millSourcePath / 'src / 'test / 'resources
    }
  }

  trait Benchmarks extends JmhModule with GenDirs {

    final override def moduleType = outer.moduleType

    final override def moduleName = outer.moduleName

    override def moduleDeps = Seq(outer)

    override def sources = T.sources(
      millSourcePath / 'src / 'test_perf / 'scala
    )

    override def millSourcePath = outer.millSourcePath

    override def resources = T.sources {
      millSourcePath / 'src / 'test_perf / 'resources
    }
  }

}