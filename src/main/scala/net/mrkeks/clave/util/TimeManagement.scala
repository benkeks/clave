package net.mrkeks.clave.util

import scala.scalajs.js
import scala.collection.mutable.Buffer


trait TimeManagement {
  var lastFrameTime = js.Date.now

  /** Time that passed since the last frame. (in ms) */
  var deltaTime = 0.0
  
  /** how many milliseconds constitute a tick of the game logic. */
  val tickTime = 10.0
  
  /** how many ticks have to be computed to catch up with the game time*/
  var tickBalance = 0.0

  private val scheduledActions = Buffer[(Double, () => Unit)]()
  
  def updateTime(timeStamp: Double) = {
    deltaTime = js.Date.now - lastFrameTime
    if (deltaTime > 500) {
      // if more than half a second has passed, there is no point in trying to catch up. (probably due to switched app or tab)
      deltaTime = 0
    }
    lastFrameTime = js.Date.now
    while (scheduledActions.headOption.exists(_._1 <= lastFrameTime)) {
      scheduledActions.remove(0)._2()
    }
  }

  def tickedTimeLoop(loopBody: =>Unit) = {
    tickBalance += deltaTime
    while (tickBalance >= tickTime) {
      tickBalance -= tickTime
      loopBody
    }
  }

  def schedule(time: Double)(action: () => Unit) = {
    val pos = if (scheduledActions.isEmpty) 0 else scheduledActions.indexWhere(_._1 > time)
    scheduledActions.insert(pos, (time, action))
  }
}
