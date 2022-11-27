package net.mrkeks.clave.util

object Mathf {
  
  /** Generate a zig zag function
   *        ____1
   *  ... /\____0
   *        \/_-1
   *    0|    |1
   */     
  def pingpong(xx: Double) = {
    val x = xx % 1.0
    if (x < .25) {
      4.0 * x
    } else if (x < .75) {
      2.0 - 4.0 * x
    } else {
      -4.0 + 4.0 * x
    }
  }

  def approach(src: Double, tar: Double, speed: Double, wraparound: Double = 0.0) = {
    var diff = tar - src
    if (wraparound != 0 && diff > wraparound * .5) diff -= wraparound
    if (wraparound != 0 && diff < -wraparound * .5) diff += wraparound
    if (Math.abs(diff) <= speed) tar else src + speed * Math.signum(diff)
  }

  def lerp(src: Double, tar: Double, ratio: Double) = {
    src * (1.0 - ratio) + tar * ratio
  }

  def quadTo(apexX: Double, ratio: Double) = {
    (1.0 / (1.0-2.0*apexX)) * ratio * ratio + ((-2.0 * apexX) / (1.0-2.0*apexX)) * ratio
  }

  def clamp(value: Double, min: Double, max: Double) = {
    Math.min(max, Math.max(value, min))
  }
}