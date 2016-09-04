package net.mrkeks.clave.game

import net.mrkeks.clave.map.GameMap
import org.denigma.threejs.Vector3

trait PositionedObject extends GameObjectData {
  protected val map: GameMap
  
  var positionOnMap = (0,0)
  
  val position = new Vector3()
  
  def updatePositionOnMap() {
    map.updateObjectPosition(this)
  }
}