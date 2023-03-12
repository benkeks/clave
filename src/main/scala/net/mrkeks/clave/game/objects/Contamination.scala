package net.mrkeks.clave.game.objects

import org.denigma.threejs.Mesh
import org.denigma.threejs.MeshLambertMaterial
import org.denigma.threejs.TetrahedronGeometry

import net.mrkeks.clave.game.abstracts.GameObject
import net.mrkeks.clave.game.abstracts.PositionedObject
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.util.Mathf

object Contamination {
  private val material = new MeshLambertMaterial()
  material.emissive.setHex(0xaacc33)
  material.color.setHex(0x111111)
  material.transparent = true
  material.depthWrite = false

  private val cloud = new TetrahedronGeometry(.8, 3)

  def clear(): Unit = {
    material.dispose()
    cloud.dispose()
  }
}

class Contamination(protected val map: GameMap)
  extends GameObject with PositionedObject with ContaminationData {

  val material = Contamination.material.clone()
  val mesh = new Mesh(Contamination.cloud, material)
  var context: DrawingContext = null

  def init(context: DrawingContext): Unit = {
    context.scene.add(mesh)
    this.context = context
    mesh.position.copy(visualPosition)
    mesh.rotation.x = Math.random() * 6.3
    mesh.rotation.y = Math.random() * 6.3
    material.opacity = creationProgress * .7
  }

  def clear(context: DrawingContext): Unit = {
    context.scene.remove(mesh)
    material.dispose()
  }

  def update(deltaTime: Double): Unit = {
    if (creationProgress < 1) {
      creationProgress += deltaTime * .001
      material.opacity = creationProgress * .7
      position.setY(-.5 + creationProgress * .25)
    } else {
      timeToLive -= deltaTime * .001
      if (timeToLive <= 0) {
        removeFromMap()
        markForDeletion()
      } else if (timeToLive <= 1) {
        material.opacity = timeToLive * .7
      }
    }
    mesh.position.x = Mathf.approach(mesh.position.x, position.x, deltaTime * .005)
    mesh.position.y = Mathf.approach(mesh.position.y, position.y, deltaTime * .005)
    mesh.position.z = Mathf.approach(mesh.position.z, position.z, deltaTime * .005)
    mesh.rotation.y += deltaTime * .001
  }
}