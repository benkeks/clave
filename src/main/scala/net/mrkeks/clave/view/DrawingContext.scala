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
  
  val scene = new Scene()
  
  camera.position.z = 14
	camera.position.y = 20
	camera.position.x = 0
	camera.lookAt(new Vector3())

  val ambient = new AmbientLight(0x404246)
	scene.add(ambient)
	
	val light = new PointLight(0xffffff, 1.0)
  light.position.set(5,10.0,5)
  scene.add(light)
  
  def render() = {
    renderer.render(scene, camera)
  }

  def adjustCameraForMap(mapWidth: Int, mapHeight: Int) = {
    camera.position.z = .5 * mapHeight + 6
	  camera.position.y = 20
	  camera.position.x = .5 * mapWidth - 8
  }
}