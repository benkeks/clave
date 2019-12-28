package net.mrkeks.clave.game

import net.mrkeks.clave.map.GameMap
import org.denigma.threejs.Vector3

trait PositionedObject extends PositionedObjectData {
  protected val map: GameMap
  
  def setPosition(newPosition: Vector3): Unit = {
    position.copy(newPosition)
    updatePositionOnMap()
  }
  
  def setPosition(x: Double, y: Double, z: Double): Unit = {
    position.set(x, y, z)
    updatePositionOnMap()
  }
  
  def updatePositionOnMap(): Unit = {
    positionOnMap = map.updateObjectPosition(this, position)
  }
  
  var touching: Option[PositionedObject] = None
  
  def touch(o: PositionedObject): Unit = {
    this touchedBy o
    o touchedBy this
  }
  
  def mutualUntouch(): Unit = {
    touching.foreach(_.untouch())
    untouch()
  }
  
  private def untouch(): Unit = {
    touching = None
  }
  
  def touchedBy(o: PositionedObject): Unit = {
    touching.foreach(_.untouch())
    touching = Some(o)
  }
}