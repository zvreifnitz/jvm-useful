import mill.scalalib._

object Versions {
  val jmh = "1.27"
}

object Deps {
  val jmh_core = Dep.parse(s"org.openjdk.jmh:jmh-core:${Versions.jmh}")
  val jmh_generator_bytecode = Dep.parse(s"org.openjdk.jmh:jmh-generator-bytecode:${Versions.jmh}")
  val jmh_generator_annprocess = Dep.parse(s"org.openjdk.jmh:jmh-generator-annprocess:${Versions.jmh}")
}