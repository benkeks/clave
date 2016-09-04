package net.mrkeks.clave.game

import net.mrkeks.clave.map.GameMap
import org.denigma.threejs.Vector3

trait PositionedObjectData  {
  
  var positionOnMap = (0,0)
  
  val position = new Vector3()
  
}