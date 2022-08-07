package net.mrkeks.clave.view

import scala.collection.mutable.HashMap
import scala.scalajs.js

import org.denigma.threejs
import org.denigma.threejs.Scene
import org.denigma.threejs.PointsMaterial
import org.denigma.threejs.Points
import org.denigma.threejs.BufferGeometry
import org.denigma.threejs.Texture
import org.denigma.threejs.{Vector3, Vector4}

import net.mrkeks.clave.game.GameObject
import net.mrkeks.clave.util.Mathf

object ParticleSystem {

  object BurstKind extends Enumeration {
    val Radial, Box = Value
  }
  type BurstKind = BurstKind.Value

}


class ParticleSystem(context: DrawingContext) {

  class ParticleType(material: threejs.Material, maxAmount: Int = 1000) extends GameObject {

    private var currentCount = 0

    private var gravity = 0.0f
    private var growth = 0.0f
    private var decay = .0001f

    val geometry = new BufferGeometry()

    private val positions = new js.typedarray.Float32Array(maxAmount * 3)
    private val directions = new js.typedarray.Float32Array(maxAmount * 3)
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
      val deltaTimeF = deltaTime.toFloat
      var writePoint = 0
      for (i <- 0 until currentCount) {
        val dx = directions(i*3 + 0)
        val dy = directions(i*3 + 1) + gravity * deltaTimeF
        val dz = directions(i*3 + 2)
        val x = positions(i*3 + 0) + deltaTimeF * dx
        val y = positions(i*3 + 1) + deltaTimeF * dy
        val z = positions(i*3 + 2) + deltaTimeF * dz
        val r = colors(i*4 + 0)
        val g = colors(i*4 + 1)
        val b = colors(i*4 + 2)
        val a = colors(i*4 + 3)
        val size = sizes(i) + deltaTimeF * growth
        if (a >= .001) {
          positions(writePoint*3 + 0) = x
          positions(writePoint*3 + 1) = y
          positions(writePoint*3 + 2) = z
          directions(writePoint*3 + 0) = dx
          directions(writePoint*3 + 1) = dy
          directions(writePoint*3 + 2) = dz
          colors(writePoint*4 + 0) = r
          colors(writePoint*4 + 1) = g
          colors(writePoint*4 + 2) = b
          colors(writePoint*4 + 3) = a - decay * deltaTimeF
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
      geometry.setDrawRange(0,currentCount)
    }

    def setGravity(gravity: Double) = {
      this.gravity = gravity.toFloat
      this
    }

    def setGrowth(growSpeed: Double) = {
      this.growth = growSpeed.toFloat
      this
    }

    def setDecay(decaySpeed: Double) = {
      this.decay = decaySpeed.toFloat
      this
    }

    def addParticle(x: Double, y: Double, z: Double, dx: Double, dy: Double, dz: Double, r: Double, g: Double, b: Double, a: Double, size: Double) = {
      if (currentCount < maxAmount) {

        positions(currentCount * 3+0) = x.toFloat
        positions(currentCount * 3+1) = y.toFloat
        positions(currentCount * 3+2) = z.toFloat

        directions(currentCount * 3+0) = dx.toFloat
        directions(currentCount * 3+1) = dy.toFloat
        directions(currentCount * 3+2) = dz.toFloat

        colors(currentCount * 4+0) = r.toFloat
        colors(currentCount * 4+1) = g.toFloat
        colors(currentCount * 4+2) = b.toFloat
        colors(currentCount * 4+3) = a.toFloat

        sizes(currentCount) = size.toFloat

        currentCount += 1
      }
    }
  }

  val particleTypes = new HashMap[String,ParticleType]()

  def registerParticleType(textureUrl: String, typeKey: String): ParticleType = {

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
    particleTypes(typeKey) = particleType
    particleType
  }

  def update(deltaTime: Double): Unit = {
    particleTypes.values.foreach(_.update(deltaTime))
  }

  def emitParticle(typeKey: String, x: Double, y: Double, z: Double, dx: Double, dy: Double, dz: Double, r: Double, g: Double, b: Double, a: Double, size: Double) = {
    particleTypes(typeKey).addParticle(x, y, z, dx, dy, dz, r, g, b, a, size)
  }

  def burst(
      typeKey: String,
      amount: Int,
      burstKind: ParticleSystem.BurstKind,
      minPos: Vector3, maxPos: Vector3,
      baseDir: Vector3, dirVariation: Vector3,
      minColor: Vector4, maxColor: Vector4,
      minSize: Double, maxSize: Double) = {
    val particleType = particleTypes(typeKey)
    burstKind match {
      case ParticleSystem.BurstKind.Box =>
        for (i <- 0 until amount) {
          val x = Mathf.lerp(minPos.x, maxPos.x, Math.random())
          val y = Mathf.lerp(minPos.y, maxPos.y, Math.random())
          val z = Mathf.lerp(minPos.z, maxPos.z, Math.random())
          val dx = Mathf.lerp(baseDir.x, dirVariation.x, Math.random())
          val dy = Mathf.lerp(baseDir.y, dirVariation.y, Math.random())
          val dz = Mathf.lerp(baseDir.z, dirVariation.z, Math.random())
          val r = Mathf.lerp(minColor.x, maxColor.x, Math.random())
          val g = Mathf.lerp(minColor.y, maxColor.y, Math.random())
          val b = Mathf.lerp(minColor.z, maxColor.z, Math.random())
          val a = Mathf.lerp(minColor.w, maxColor.w, Math.random())
          val size = Mathf.lerp(minSize, maxSize, Math.random())
          particleType.addParticle(x, y, z, dx, dy, dz, r, g, b, a, size)
        }
      case ParticleSystem.BurstKind.Radial =>
        for (i <- 0 until amount) {
          val deg = 2 * Math.PI * i / amount
          val x = Mathf.lerp(minPos.x, maxPos.x, Math.random())
          val y = Mathf.lerp(minPos.y, maxPos.y, Math.random())
          val z = Mathf.lerp(minPos.z, maxPos.z, Math.random())
          val dx = baseDir.x + Math.cos(deg) * dirVariation.x
          val dy = Mathf.lerp(baseDir.y, dirVariation.y, Math.random())
          val dz = baseDir.z + Math.sin(deg) * dirVariation.z
          val r = Mathf.lerp(minColor.x, maxColor.x, Math.random())
          val g = Mathf.lerp(minColor.y, maxColor.y, Math.random())
          val b = Mathf.lerp(minColor.z, maxColor.z, Math.random())
          val a = Mathf.lerp(minColor.w, maxColor.w, Math.random())
          val size = Mathf.lerp(minSize, maxSize, Math.random())
          particleType.addParticle(x, y, z, dx, dy, dz, r, g, b, a, size)
        }
    }
  }

}
