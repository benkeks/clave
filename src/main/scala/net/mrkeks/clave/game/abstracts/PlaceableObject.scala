package net.mrkeks.clave.game.abstracts

import net.mrkeks.clave.map.GameMap
import org.denigma.threejs.Vector3

trait PlaceableObject extends PositionedObject {

  protected val map: GameMap

  def place(x: Int, z: Int): Boolean = {
    if (canBePlaced(x, z)) {
      setPosition(x, 0, z)
      true
    } else {
      false
    }
  }
  
  def canBePlaced(x: Int, z: Int): Boolean =
    !map.intersectsLevel(x, z, considerObstacles = true)
}