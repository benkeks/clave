package net.mrkeks.clave.game

import net.mrkeks.clave.map.GameMap
import org.denigma.threejs.Vector3

trait PositionedObject extends PositionedObjectData {
  protected val map: GameMap
  
  def setPosition(newPosition: Vector3) {
    position.copy(newPosition)
    updatePositionOnMap()
  }
  
  def setPosition(x: Double, y: Double, z: Double) {
    position.set(x, y, z)
    updatePositionOnMap()
  }
  
  def updatePositionOnMap() {
    positionOnMap = map.updateObjectPosition(this, position)
  }
  
  var touching: Option[PositionedObject] = None
  
  def touch(o: PositionedObject) {
    this touchedBy o
    o touchedBy this
  }
  
  def mutualUntouch() {
    touching.foreach(_.untouch())
    untouch()
  }
  
  private def untouch() {
    touching = None
  }
  
  def touchedBy(o: PositionedObject) {
    touching.foreach(_.untouch())
    touching = Some(o)
  }
}