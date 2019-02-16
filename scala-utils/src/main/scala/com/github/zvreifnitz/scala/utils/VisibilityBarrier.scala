package com.github.zvreifnitz.scala.utils

import com.github.zvreifnitz.java.utils.{VisibilityBarrier => jVisibilityBarrier}

object VisibilityBarrier {

  def visible[T](input: T): T = jVisibilityBarrier.makeVisible(input)

  def visible[T](op: => T): T = jVisibilityBarrier.makeVisible(op)
}
