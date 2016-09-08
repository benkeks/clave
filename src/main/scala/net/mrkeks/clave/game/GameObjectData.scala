package net.mrkeks.clave.game

import org.denigma.threejs.Vector3

trait GameObjectData {
  /** The id will be assigned once the game object is added to the game. */
  var id: Int = -1
  
  override def equals(o: Any) = o match {
    case that: GameObjectData => this.id == that.id
    case _ => false
  }
}