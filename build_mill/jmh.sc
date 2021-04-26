import $file.deps
import deps.Deps._
import mill._
import mill.define.{Target, TaskModule}
import mill.modules._
import mill.scalalib._
import os.Path

trait JmhModule extends JavaModule with TaskModule {

  def jmhGeneratorDeps = resolveDeps(T {
    Agg(jmh_generator_bytecode)
  })

  override def defaultCommandName() = "runBenchmark"

  override def ivyDeps: Target[Agg[Dep]] = T {
    super.ivyDeps() ++ Agg(jmh_core)
  }

  def runBenchmarkWithPerfNorm(args: String*) = T.command {
    val a = args.toList ++ List("-prof", "perfnorm")
    runBenchmark(a: _*)
  }

  def runBenchmarkWithPerfAsm(args: String*) = T.command {
    val a = args.toList ++ List("-prof", "perfasm")
    runBenchmark(a: _*)
  }

  def runBenchmark(args: String*) = T.command {
    val (_, resourcesDir) = generateJmhJavaSourceFiles()
    Jvm.runSubprocess(
      "org.openjdk.jmh.Main",
      classPath = (runClasspath() ++ jmhGeneratorDeps()).map(_.path) ++
        Seq(compileJmhJavaSourceFiles().path, resourcesDir),
      mainArgs = args,
      workingDir = T.ctx.dest
    )
  }

  def compileJmhJavaSourceFiles = T {
    val dest = T.ctx.dest
    val (sourcesDir, _) = generateJmhJavaSourceFiles()
    val sources = os.walk(sourcesDir).filter(os.isFile)
    os.proc("javac",
      sources.map(_.toString),
      "-cp",
      (runClasspath() ++ jmhGeneratorDeps()).map(_.path.toString).mkString(":"),
      "-d",
      dest).call(dest)
    PathRef(dest)
  }

  def generateJmhJavaSourceFiles = T {
    val dest = T.ctx.dest
    val sourcesDir = prepareDir(dest / 'jmh_src)
    val resourcesDir = prepareDir(dest / 'jmh_res)
    Jvm.runSubprocess(
      "org.openjdk.jmh.generators.bytecode.JmhBytecodeGenerator",
      (runClasspath() ++ jmhGeneratorDeps()).map(_.path),
      mainArgs = Array(
        compile().classes.path,
        sourcesDir,
        resourcesDir,
        "default"
      ).map(_.toString)
    )
    (sourcesDir, resourcesDir)
  }

  private[this] def prepareDir(dir: Path): Path = {
    os.remove.all(dir)
    os.makeDir.all(dir)
    dir
  }
}