package net.mrkeks.clave.game.objects

import net.mrkeks.clave.map.GameMap
import org.denigma.threejs.MeshLambertMaterial
import org.denigma.threejs.BoxGeometry
import org.denigma.threejs.Mesh
import net.mrkeks.clave.game.GameObject
import net.mrkeks.clave.game.Player
import net.mrkeks.clave.game.PositionedObject
import net.mrkeks.clave.game.PositionedObjectData
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.map.MapData
import net.mrkeks.clave.game.Monster

object Crate {
  private val material = new MeshLambertMaterial()
  material.color.setHex(0xdddd99)
  
  private val box = new BoxGeometry(.95, .95, .95)
  
  def clear() {
    material.dispose()
    box.dispose()
  }
}

class Crate(protected val map: GameMap)
  extends GameObject with PositionedObject with CrateData {
  
  import CrateData._
  
  val mesh = new Mesh(Crate.box, Crate.material)
  
  def init(context: DrawingContext) {
    mesh.rotateY(.1 - .2 * Math.random())
    context.scene.add(mesh)
  }
  
  def clear(context: DrawingContext) {
    context.scene.remove(mesh)
  }
  
  def update(deltaTime: Double) {
    state match {
      case Standing() =>
        position.setY(if (touching.isEmpty) 0 else .1)
      case Carried(player) =>
        val dir = PositionedObjectData.Direction.toVec3(player.viewDirection)
          .multiplyScalar(.2)
          .setY(.5)
        position.copy(dir add player.getPosition)
    }
    mesh.position.copy(position)
    //mesh.position.lerp(position, .5)
  }
  
  def pickup(player: Player) {
    state = Carried(player)
    
    // temporally move to fictional position in order to remove location from game map
    position.setZ(-10)
    updatePositionOnMap()
  }
  
  def place(x: Int, z: Int) = {
    if (!map.intersectsLevel(x, z) 
        && !map.getObjectsAt((x,z)).exists {
             case _: Monster | _: Gate => true
             case _ => false
           }) {
      setPosition(x, 0, z)
      state = Standing()
      true
    } else {
      false
    }
  }
}