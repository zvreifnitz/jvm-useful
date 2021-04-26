package com.github.zvreifnitz.score.concurrent

import com.github.zvreifnitz.jcore.concurrent.{VisibilityBarrier => jVisibilityBarrier}

object VisibilityBarrier {
  def visible[T](instance: T): T = jVisibilityBarrier.makeVisible(instance)

  def visible[T](instance: => T): T = jVisibilityBarrier.makeVisible(instance)
}