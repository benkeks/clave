package net.mrkeks.clave.view

import scala.collection.mutable.Queue
import scala.scalajs.js

import org.denigma.threejs
import org.denigma.threejs.Scene
import org.denigma.threejs.PointsMaterial
import org.denigma.threejs.Points
import org.denigma.threejs.BufferGeometry
import org.denigma.threejs.Texture

class ParticleSystem(context: DrawingContext) {

  class ParticleType(material: threejs.Material, maxAmount: Int = 1000) {
    val geometry = new BufferGeometry()
    val positions = new js.typedarray.Float32Array(maxAmount * 3)
    val colors = new js.typedarray.Float32Array(maxAmount * 4)

    for (i <- 0 to maxAmount * 3 ) {
      positions(i) = (50 * Math.random() - 25).toFloat
    }
    for (i <- 0 to maxAmount ) {
      colors(i) = Math.random().toFloat
      colors(i+1) = Math.random().toFloat
      colors(i+2) = Math.random().toFloat
      colors(i+3) = 0.5.toFloat
    }
    val positionAttribute = new threejs.Float32BufferAttribute(positions, 3)
    //should speed up changes...
    positionAttribute.asInstanceOf[js.Dynamic].setUsage(threejs.THREE.asInstanceOf[js.Dynamic].DynamicDrawUsage)
    geometry.setAttribute("position", positionAttribute)
    geometry.setAttribute("color", new threejs.Float32BufferAttribute(colors, 4));
    val mesh = new Points(geometry, material)
    context.scene.add(mesh)
  }

  val particleTypes = new Queue[ParticleType]()

  def registerParticleType(textureUrl: String) = {

    val material = new PointsMaterial()
    material.size = 10
    material.depthWrite = false
    material.vertexColors = true
    DrawingContext.textureLoader.load(textureUrl, { tex: Texture =>
      material.alphaMap = tex
      material.opacity = .9
      material.transparent = true
      material.needsUpdate = true
    })

    particleTypes.append(new ParticleType(material))
  }

}
