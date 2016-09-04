package net.mrkeks.clave.game

import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.map.GameMap
import org.denigma.threejs.MeshLambertMaterial
import org.denigma.threejs.BoxGeometry
import org.denigma.threejs.Mesh

object Crate {
  private val material = new MeshLambertMaterial()
  material.color.setHex(0xdddd99)
  
  private val box = new BoxGeometry(1, 1, 1)
  
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
    context.scene.add(mesh)
  }
  
  def clear() {
  }
  
  def update(deltaTime: Double) {
    state match {
      case Standing() =>
        position.setY(if (touching.isEmpty) 0 else .1)
      case Carried(player) =>
        val dir = PlayerData.Direction.toVec3(player.viewDirection)
          .multiplyScalar(.2)
          .setY(.5)
        position.copy(dir add player.position)
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
    if (!map.intersectsLevel(x, z)) {
      position.set(x, 0, z)
      state = Standing()
      updatePositionOnMap()
      true
    } else {
      false
    }
  }
}