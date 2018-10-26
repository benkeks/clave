package net.mrkeks.clave.game

import net.mrkeks.clave.view.DrawingContext
import org.denigma.threejs.SpriteMaterial
import org.denigma.threejs.PlaneGeometry
import org.denigma.threejs.Mesh
import org.denigma.threejs.MeshLambertMaterial
import org.denigma.threejs.ImageUtils
import org.denigma.threejs.TextureLoader
import org.denigma.threejs.Texture
import org.denigma.threejs.THREE
import net.mrkeks.clave.map.GameMap

object ObjectShadow {
  
  val material = new MeshLambertMaterial()
  material.color.setHex(0x888888)
  material.blending = THREE.SubtractiveBlending
  material.transparent = true
  material.depthWrite = false
  material.opacity = .5
  material.polygonOffset = true
  material.polygonOffsetUnits = -10.0

  val texture = new TextureLoader().load("gfx/shadow.gif", { tex: Texture =>
    material.map = tex
    material.needsUpdate = true
  })
    
  val geometry = new PlaneGeometry(1.7, 1.7, 1, 1)
  geometry.normalsNeedUpdate = true
  
  def clear() {
    material.dispose()
    geometry.dispose()
  }
}

trait ObjectShadow {
  self: PositionedObject =>
  
  val shadowSize: Double
  
  private val towel = new Mesh(ObjectShadow.geometry, ObjectShadow.material)
  
  def initShadow(context: DrawingContext) {
    towel.rotateX(-Math.PI * .5)
    towel.scale.set(shadowSize, shadowSize, shadowSize)
    context.scene.add(towel)
  }
    
  def updateShadow() {
    towel.position.set(position.x + position.y*.4 + 0.3, -0.5, position.z)
  }
  
  def clearShadow(context: DrawingContext) {
    context.scene.remove(towel)
  }
}