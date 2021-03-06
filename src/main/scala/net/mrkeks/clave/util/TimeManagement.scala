package net.mrkeks.clave.util

import scala.scalajs.js

trait TimeManagement {
  var lastFrameTime = js.Date.now

  /** Time that passed since the last frame. (in ms) */
  var deltaTime = 0.0
  
  /** how many milliseconds constitute a tick of the game logic. */
  val tickTime = 10.0
  
  /** how many ticks have to be computed to catch up with the game time*/
  var tickBalance = 0.0
  
  def updateTime(timeStamp: Double) = {
    deltaTime = js.Date.now - lastFrameTime
    if (deltaTime > 500) {
      // if more than half a second has passed, there is no point in trying to catch up. (probably due to switched app or tab)
      deltaTime = 0
    }
    lastFrameTime = js.Date.now
  }

  def tickedTimeLoop(loopBody: =>Unit) = {
    tickBalance += deltaTime
    while (tickBalance >= tickTime) {
      tickBalance -= tickTime
      loopBody
    }
  }
}
