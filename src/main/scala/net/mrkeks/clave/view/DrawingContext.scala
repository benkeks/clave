package net.mrkeks.clave.view

import org.scalajs.dom
import org.scalajs.dom.raw.CanvasRenderingContext2D
import org.scalajs.dom.html.Canvas
import org.denigma.threejs.WebGLRenderer
import org.denigma.threejs.WebGLRendererParameters
import org.denigma.threejs.OrthographicCamera
import org.denigma.threejs.Scene
import org.denigma.threejs.MeshBasicMaterial
import org.denigma.threejs.BoxGeometry
import org.denigma.threejs.MeshBasicMaterialParameters
import org.denigma.threejs.Mesh
import org.denigma.threejs.DirectionalLight
import org.denigma.threejs.Color
import org.denigma.threejs.Vector3
import org.denigma.threejs.PointLight
import org.denigma.threejs.AmbientLight

class DrawingContext() {
  
  val width = dom.window.innerWidth
  val height = dom.window.innerHeight
  
  val renderer = new WebGLRenderer()
  
  renderer.setSize(width, height)
  dom.document.body.appendChild( renderer.domElement )
  
  renderer.setClearColor(new Color(0x20a010));
  
  val camera = new OrthographicCamera(0,16,2,-14,-100,100)
  
  val scene = new Scene();
  
  // |   
  // |  --.
  // |___\.
  camera.position.z = 15
	camera.position.y = 20
	camera.position.x = 0
	camera.lookAt(new Vector3())
	
  val ambient = new AmbientLight(0x303236)
	scene.add(ambient)
	
	val light = new PointLight(0xffffff, 1.0)
  light.position.set(5,10.0,5)
  scene.add(light)
  
  
  def render() = renderer.render(scene, camera)
}