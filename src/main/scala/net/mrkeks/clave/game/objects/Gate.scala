package net.mrkeks.clave.game.objects

import org.denigma.threejs.BoxGeometry
import org.denigma.threejs.Mesh
import org.denigma.threejs.MeshLambertMaterial

import net.mrkeks.clave.game.GameObject
import net.mrkeks.clave.game.PositionedObject
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.view.DrawingContext

object Gate {
  private val material = new MeshLambertMaterial()
  material.color.setHex(0x8899aa)
  
  private val box = new BoxGeometry(.95, .1, .95)
  
  def clear() {
    material.dispose()
    box.dispose()
  }
}

class Gate(protected val map: GameMap)
  extends GameObject with PositionedObject with GateData {
  
  import GateData._
  
  val mesh = new Mesh(Gate.box, Gate.material)
  
  def init(context: DrawingContext) {
    position.setY(-.5)
    context.scene.add(mesh)
  }
  
  def clear(context: DrawingContext) {
    context.scene.remove(mesh)
  }
  
  def update(deltaTime: Double) {
    position.setY(-.5)
    mesh.position.copy(position)
  }
}