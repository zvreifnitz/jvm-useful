import $file.build_mill.base_module
import $file.build_mill.deps
import base_module.{JModule, SModule}
import deps.Deps._
import mill.define.Target
import mill.scalalib.{Dep, JavaModule}
import mill.{Agg, T}

object jCore extends JModule {

  object test extends Tests

  object benchmark extends Benchmarks

}

object jPipeline extends JModule {
  override def moduleDeps: Seq[JavaModule] = Seq(jCore)

  object test extends Tests

  object benchmark extends Benchmarks

}

object sCore extends SModule {
  override def moduleDeps: Seq[JavaModule] = Seq(jCore)

  object test extends Tests

  object benchmark extends Benchmarks

}