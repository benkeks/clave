package net.mrkeks.clave.game

import net.mrkeks.clave.map.GameMap
import org.denigma.threejs.Vector3

trait PositionedObjectData  {
  
  protected var positionOnMap = (-1,-1)
  
  protected val position = new Vector3()
  
  /** returns a copy of the internal position information */
  def getPosition = position.clone()
  
  /** returns the position on the map if the entity is placed on the map */
  def getPositionOnMap = {
    if (positionOnMap._1 >= 0 && positionOnMap._2 >= 0) {
      Some(positionOnMap)
    } else {
      None
    }
  }
}