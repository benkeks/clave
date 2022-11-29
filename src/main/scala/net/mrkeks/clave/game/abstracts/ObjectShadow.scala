package net.mrkeks.clave.game.abstracts

import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.util.Mathf

import org.denigma.threejs.SpriteMaterial
import org.denigma.threejs.PlaneGeometry
import org.denigma.threejs.Mesh
import org.denigma.threejs.MeshLambertMaterial
import org.denigma.threejs.ImageUtils
import org.denigma.threejs.TextureLoader
import org.denigma.threejs.Texture
import org.denigma.threejs.THREE
import org.denigma.threejs.InstancedMesh
import org.denigma.threejs.{Matrix4, Vector3}
import org.denigma.threejs.Color

import scala.collection.mutable.Queue

object ObjectShadow {
  
  val material = new MeshLambertMaterial()
  material.color.setHex(0x888888)
  material.transparent = true
  material.depthWrite = false
  material.opacity = .5
  material.polygonOffset = true
  material.polygonOffsetUnits = -10.0

  val texture = new TextureLoader().load("gfx/shadow_inv.png", { tex: Texture =>
    material.map = tex
    material.needsUpdate = true
  })

  val geometry = new PlaneGeometry(1.4, 1.4, 1, 1).rotateX(-Math.PI * .5)

  private val shadowCasters = Queue[ObjectShadow]()

  private val MaxShadowCount = 50
  private val shadows = new InstancedMesh(geometry, material, MaxShadowCount)
  shadows.renderOrder = 2

  def init(context: DrawingContext) = {
    context.scene.add(shadows)
  }

  def updateAllShadows() = {
    var i = 0
    val posMatrix = new Matrix4()
    val scaleVector = new Vector3()
    val color = new Color()
    for {
      s <- shadowCasters
    } {
      val pos = s.getPosition
      posMatrix.makeTranslation(pos.x + pos.y*.4 + 0.1, -0.45, pos.z)
      scaleVector.set(s.shadowXSize, s.shadowXSize, s.shadowZSize)
      posMatrix.scale(scaleVector)
      shadows.setMatrixAt(i, posMatrix)
      color.r = Mathf.clamp(pos.y *.4 - 1.0, 0.0, 0.5)
      color.g = color.r
      color.b = color.r
      shadows.setColorAt(i, color)
      i += 1
    }
    shadows.count = i
    shadows.instanceMatrix.needsUpdate = true
    shadows.instanceColor.needsUpdate = true
  }

  def clear(): Unit = {
    material.dispose()
    geometry.dispose()
  }
}

trait ObjectShadow extends PositionedObject {

  def shadowXSize: Double
  def shadowZSize: Double
  
  def initShadow(context: DrawingContext): Unit = {
    ObjectShadow.shadowCasters += this
  }

  def clearShadow(context: DrawingContext): Unit = {
    ObjectShadow.shadowCasters -= this
  }
}