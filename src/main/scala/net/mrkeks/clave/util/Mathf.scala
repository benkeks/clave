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
}