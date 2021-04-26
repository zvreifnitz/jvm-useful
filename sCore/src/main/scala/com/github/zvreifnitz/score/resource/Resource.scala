package com.github.zvreifnitz.score.resource

object Resource {
  def using[C <: AutoCloseable, R](resource: => C)(f: C => R): R = {
    var c: C = null.asInstanceOf[C]
    try {
      c = resource
      f(c)
    } finally {
      closeQuietly(c)
    }
  }

  def closeQuietly[C <: AutoCloseable](resource: C): Unit = {
    try {
      if (resource != null) {
        resource.close()
      }
    } catch {
      case _: Exception =>
    }
  }
}
