package net.mrkeks.clave.game.objects

import org.denigma.threejs.BoxGeometry
import org.denigma.threejs.Mesh
import org.denigma.threejs.MeshLambertMaterial

import net.mrkeks.clave.game.GameObject
import net.mrkeks.clave.game.PositionedObject
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.view.DrawingContext

object Trigger {
  private val material = new MeshLambertMaterial()
  material.color.setHex(0x3355aa)
  
  private val box = new BoxGeometry(1.06, .1, 1.06)
  
  def clear() {
    material.dispose()
    box.dispose()
  }
}

class Trigger(protected val map: GameMap)
  extends GameObject with PositionedObject with TriggerData {
  
  import TriggerData._
  
  val mesh = new Mesh(Trigger.box, Trigger.material)
  
  def init(context: DrawingContext) {
    setState(Idle())
    context.scene.add(mesh)
  }
  
  def clear(context: DrawingContext) {
    context.scene.remove(mesh)
  }
  
  def update(deltaTime: Double) {
    
    state match {
      case Idle() =>
        val objectsAbove = map.getObjectsAt(positionOnMap)
        // the collection also contains `this` object
        if (objectsAbove.size > 1) {
          setState(Pushed((objectsAbove - this).head))
        }
      case Pushed(by) =>
        if (by.getPositionOnMap != Some(positionOnMap)) {
          setState(Idle())
        }
    }
    
    mesh.position.copy(position)
  }
  
  def setState(newState: State) {
    state = newState match {
      case Idle() =>
        position.setY(-.45)
        newState
      case Pushed(by) =>
        position.setY(-.52)
        newState
    }
  }
}