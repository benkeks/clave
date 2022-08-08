package net.mrkeks.clave.game.abstracts

import org.denigma.threejs.Vector3

trait GameObjectData {
  /** The id will be assigned once the game object is added to the game. */
  var id: Int = -1

  protected[game] var markedForDeletion: Boolean = false
  
  /** As it's usually not a good idea to delete objects from the scene while updating it, they can be marked for clean up after the update.*/
  def markForDeletion(): Unit = {
    markedForDeletion = true
  }

  override def equals(o: Any) = o match {
    case that: GameObjectData => this.id == that.id
    case _ => false
  }
}