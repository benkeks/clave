package net.mrkeks.clave.view

import scala.collection.mutable.Queue
import scala.scalajs.js

import org.denigma.threejs
import org.denigma.threejs.Scene
import org.denigma.threejs.PointsMaterial
import org.denigma.threejs.Points
import org.denigma.threejs.BufferGeometry
import org.denigma.threejs.Texture
import org.denigma.threejs.{Vector3, Vector4}
import net.mrkeks.clave.game.GameObject

class ParticleSystem(context: DrawingContext) {

  class ParticleType(material: threejs.Material, maxAmount: Int = 1000) extends GameObject {

    var currentCount = 0

    val geometry = new BufferGeometry()

    private val positions = new js.typedarray.Float32Array(maxAmount * 3)
    private val colors = new js.typedarray.Float32Array(maxAmount * 4)
    private val sizes = new js.typedarray.Float32Array(maxAmount)

    private val positionAttribute = new threejs.Float32BufferAttribute(positions, 3)
    private val colorAttribute = new threejs.Float32BufferAttribute(colors, 4)
    private val sizeAttribute = new threejs.Float32BufferAttribute(sizes, 1)
    //should speed up changes...
    positionAttribute.asInstanceOf[js.Dynamic].setUsage(threejs.THREE.asInstanceOf[js.Dynamic].DynamicDrawUsage)
    geometry.setAttribute("position", positionAttribute)
    geometry.setAttribute("color", colorAttribute)
    geometry.setAttribute("size", sizeAttribute)

    val mesh = new Points(geometry, material)
    context.scene.add(mesh)

    override def init(context: DrawingContext): Unit = {

    }

    override def clear(context: DrawingContext): Unit = {
      mesh.geometry.dispose()
      context.scene.remove(mesh)
    }

    override def update(deltaTime: Double) = {
      println("updating particles")
      val deltaTimeF = deltaTime.toFloat
      var writePoint = 0
      for (i <- 0 until currentCount) {
        val x = positions(i*3 + 0)
        val y = positions(i*3 + 1)
        val z = positions(i*3 + 2)
        val r = colors(i*4 + 0)
        val g = colors(i*4 + 1)
        val b = colors(i*4 + 2)
        val a = colors(i*4 + 3)
        val size = sizes(i)
        if (a >= .001) {
          positions(writePoint*3 + 0) = x
          positions(writePoint*3 + 1) = y
          positions(writePoint*3 + 2) = z
          colors(writePoint*4 + 0) = r
          colors(writePoint*4 + 1) = g
          colors(writePoint*4 + 2) = b
          colors(writePoint*4 + 3) = a - .0001f * deltaTimeF
          sizes(writePoint) = size
          writePoint += 1
        }
      }
      currentCount = writePoint
      positionAttribute.array = positions
      colorAttribute.array = colors
      sizeAttribute.array = sizes
      positionAttribute.needsUpdate = true
      colorAttribute.needsUpdate = true
      sizeAttribute.needsUpdate = true
    }
    
    def addParticle(x: Double, y: Double, z: Double, r: Double, g: Double, b: Double, a: Double, size: Double) = {
      if (currentCount < maxAmount) {
        positionAttribute.setXYZ(currentCount, x, y, z)
        colors(currentCount * 4+0) = r.toFloat
        colors(currentCount * 4+1) = g.toFloat
        colors(currentCount * 4+2) = b.toFloat
        colors(currentCount * 4+3) = a.toFloat
        //colorAttribute.setXYZW(currentCount, r, g, b, a)
        sizeAttribute.setX(currentCount, size)
        currentCount += 1
      }
    }
  }

  val particleTypes = new Queue[ParticleType]()

  def registerParticleType(textureUrl: String): ParticleType = {

    val material = new PointsMaterial()
    material.depthWrite = false
    material.vertexColors = true
    material.onBeforeCompile = { (shader, renderer) =>
      shader.vertexShader = shader.vertexShader.replaceFirst("uniform float size;", "attribute float size;")
    }
    DrawingContext.textureLoader.load(textureUrl, { tex: Texture =>
      material.alphaMap = tex
      material.opacity = .9
      material.transparent = true
      material.needsUpdate = true
    })
    val particleType = new ParticleType(material)
    particleTypes.append(particleType)
    particleType
  }

}
