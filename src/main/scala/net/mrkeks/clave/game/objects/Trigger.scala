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
  
  def clear(): Unit = {
    material.dispose()
    box.dispose()
  }
}

class Trigger(protected val map: GameMap)
  extends GameObject with PositionedObject with TriggerData {
  
  import TriggerData._
  
  val material = Trigger.material.clone()
  val mesh = new Mesh(Trigger.box, material)
  
  def init(context: DrawingContext): Unit = {
    setState(Idle())
    context.scene.add(mesh)
  }
  
  def clear(context: DrawingContext): Unit = {
    context.scene.remove(mesh)
    material.dispose()
  }
  
  def update(deltaTime: Double): Unit = {
    
    state match {
      case Idle() =>
        // be sure to remove the `this` object from the colection...
        val objectsAbove = map.getObjectsAt(positionOnMap).diff(Set(this))
        objectsAbove.headOption.map(o => setState(Pushed(o)))
      case Pushed(by) =>
        if (by.getPositionOnMap != Some(positionOnMap)) {
          setState(Idle())
        }
    }
    
    mesh.position.copy(position)
  }
  
  def setState(newState: State): Unit = {
    state = newState match {
      case Idle() =>
        position.setY(-.45)
        material.color.setHex(0x3355aa)
        newState
      case Pushed(by) =>
        position.setY(-.52)
        material.color.setHex(0x4477ee)
        newState
    }
  }
}