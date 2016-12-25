package net.mrkeks.clave.game.objects

import org.denigma.threejs.BoxGeometry
import org.denigma.threejs.Mesh
import org.denigma.threejs.MeshLambertMaterial
import net.mrkeks.clave.game.GameObject
import net.mrkeks.clave.game.PositionedObject
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.game.characters.Monster

object Gate {
  private val material = new MeshLambertMaterial()
  material.color.setHex(0x8899aa)
  
  private val box = new BoxGeometry(.99, 1.0, .99)
  
  def clear() {
    material.dispose()
    box.dispose()
  }
}

class Gate(protected val map: GameMap)
  extends GameObject with PositionedObject with GateData {
  
  import GateData._
  
  val mesh = new Mesh(Gate.box, Gate.material)
  
  var animY = 0.0
  
  def init(context: DrawingContext) {
    context.scene.add(mesh)
  }
  
  def clear(context: DrawingContext) {
    context.scene.remove(mesh)
  }
  
  def update(deltaTime: Double) {
    state match {
      case Open() =>
        animY = Math.max(animY - .01 * deltaTime, -.99)
      case Closing() =>
        if (animY < 0.0) {
          animY = animY + (.013 + Math.sin(position.x*.5)*.01) * deltaTime
        } else {
          setState(Closed())
          updatePositionOnMap()
        }
      case Closed() =>
        animY = 0.0
        
    }
    
    mesh.position.set(position.x, animY, position.z)
  }
  
  def open() {
    setState(Open())
    updatePositionOnMap()
  }
  
  def close() {
    if (state.isInstanceOf[Open]) {
      setState(Closing())
      updatePositionOnMap()
    }
  }
  
  def setState(newState: State) {
    state = newState
  }
}