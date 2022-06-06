package net.mrkeks.clave.view

import scala.scalajs.js
import org.scalajs.dom
import org.denigma.threejs.WebGLRenderer
import org.denigma.threejs.OrthographicCamera
import org.denigma.threejs.Scene
import org.denigma.threejs.Color
import org.denigma.threejs.Vector3
import org.denigma.threejs.PointLight
import org.denigma.threejs.AmbientLight
import org.denigma.threejs.TextureLoader
import org.denigma.threejs.GLTFLoader

object DrawingContext {
  val textureLoader = new TextureLoader()
  val gltfLoader = new GLTFLoader()
}

class DrawingContext() {

  var width: Double = 0.0
  var height: Double = 0.0
  var aspect: Double = 1.0

  var renderer: WebGLRenderer = new WebGLRenderer()
  dom.document.body.appendChild(renderer.domElement)

  renderer.setClearColor(new Color(0x604060))

  val camera = new OrthographicCamera(7 - (8 * aspect), 7 + (8 * aspect), 2,2 - 16,-100,100)
  val cameraMin = new Vector3(0, 0, 0)
  val cameraMax = new Vector3(0, 100, 28)
  val cameraLookAt = new Vector3()
  cameraUpdatePosition(new Vector3())
  adjustViewport()

  val scene = new Scene()

  val ambient = new AmbientLight(0x606266)
  scene.add(ambient)

  val light = new PointLight(0xffffff, 1.0)
  light.position.set(5,10.0,5)
  scene.add(light)

  val light2 = new PointLight(0x113311, 1.0)
  light2.position.set(-10.0,-10.0,-3)
  scene.add(light2)

  dom.window.onresize = (uiEv) => {
    adjustViewport()
  }

  def adjustViewport() = {
    width = dom.window.innerWidth
    height = dom.window.innerHeight
    aspect = width / height
    renderer.setSize(width, height)
    camera.top = 8
    camera.bottom = -8
    camera.left = - (8 * aspect)
    camera.right = + (8 * aspect)
    camera.updateProjectionMatrix()
  }

  def render() = {
    renderer.render(scene, camera)
  }

  def adjustCameraForMap(mapWidth: Int, mapHeight: Int): Unit = {
    val camSpace = 16
    if (mapWidth > camSpace || mapHeight > camSpace) {
      cameraMin.set(camSpace * .4, 0, camSpace * .4)
      cameraMax.set(mapWidth - camSpace * .4, 0, mapHeight - camSpace * .4)
    } else {
      cameraMin.set(mapWidth / 2, 0, mapHeight / 2)
      cameraMax.copy(cameraMin)
    }

  }

  def cameraUpdatePosition(lookAt: Vector3): Unit = {
    val zOff = 11
    cameraLookAt.copy(lookAt).clamp(cameraMin, cameraMax)
    val y = lookAt.y
    camera.position.copy(cameraLookAt).add(new Vector3(14 * Math.sin(.02 * y),20,zOff))
    camera.lookAt(cameraLookAt.clone().add(new Vector3(0, .5 * y, zOff * (1 - Math.cos(.02 * y)))))
  }
}