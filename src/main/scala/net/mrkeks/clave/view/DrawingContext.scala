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

  val width = dom.window.innerWidth
  val height: Double = dom.window.innerHeight

  val aspect: Double = width / height

  val renderer: WebGLRenderer = new WebGLRenderer()
  
  renderer.setSize(width, height)
  dom.document.body.appendChild(renderer.domElement)
  
  renderer.setClearColor(new Color(0x604060))

  val camera = new OrthographicCamera(7 - (8 * aspect), 7 + (8 * aspect), 2,2 - 16,-100,100)
  val cameraOffset = new Vector3(0, 20, 14)
  val cameraLookAt = new Vector3()
  cameraUpdatePosition(new Vector3())

  val scene = new Scene()

  val ambient = new AmbientLight(0x606266)
  scene.add(ambient)

  val light = new PointLight(0xffffff, 1.0)
  light.position.set(5,10.0,2)
  scene.add(light)

  val light2 = new PointLight(0x226644, 1.0)
  light2.position.set(-10,-5.0,-3)
  scene.add(light2)

  def render() = {
    renderer.render(scene, camera)
  }

  def adjustCameraForMap(mapWidth: Int, mapHeight: Int) = {
    cameraOffset.set(.5 * mapWidth - 8, 20, .5 * mapHeight + 6)
    cameraLookAt.x = .5 * mapWidth - 8
  }

  def cameraUpdatePosition(lookFrom: Vector3) = {
    camera.position.copy(lookFrom)
    camera.position.add(cameraOffset)
    cameraLookAt.y = .5 * lookFrom.y
    camera.lookAt(cameraLookAt)
  }
}