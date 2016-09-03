package net.mrkeks.clave.game

import net.mrkeks.clave.view.DrawingContext
import org.denigma.threejs.Vector3

trait GameObject {
  /** The id will be assigned once the game object is added to the game. */
  var id: Int = -1
  
  def init(context: DrawingContext)
  
  def clear()
  
  def update(deltaTime: Double)
  
  val position = new Vector3()
}