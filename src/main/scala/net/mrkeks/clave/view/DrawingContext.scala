package net.mrkeks.clave.view

import scala.scalajs.js
import org.scalajs.dom
import org.denigma.threejs.{WebGLRenderer, WebGLRendererParameters}
import org.denigma.threejs.PerspectiveCamera
import org.denigma.threejs.Scene
import org.denigma.threejs.Color
import org.denigma.threejs.Vector3
import org.denigma.threejs.PointLight
import org.denigma.threejs.AmbientLight
import org.denigma.threejs.TextureLoader
import org.denigma.threejs.GLTFLoader
import org.denigma.threejs.Fog
import net.mrkeks.clave.game.abstracts.ObjectShadow

object DrawingContext {
  val textureLoader = new TextureLoader()
  val gltfLoader = new GLTFLoader()

  val bgColor = new Color(0x604060)

  private val QualityKey = net.mrkeks.clave.game.ProgressTracking.ClavePrefix + "quality"
}

class DrawingContext() {

  var width: Double = 0.0
  var height: Double = 0.0
  var aspect: Double = 1.0

  val devicePixelRatio = dom.window.devicePixelRatio

  /** how many game space units does the camera height cover?*/
  var cameraSpace: Double = 16.0

  private var gfxDetail = loadGfxDetail()
  private val gfxConfig = makeGfxConfig()
  var renderer: WebGLRenderer = new WebGLRenderer(gfxConfig)
  dom.document.body.appendChild(renderer.domElement)

  renderer.setClearColor(DrawingContext.bgColor)

  val camera = new PerspectiveCamera(aspect = aspect)
  val cameraMin = new Vector3(0, 0, 0)
  val cameraMax = new Vector3(0, 100, 28)
  val cameraLookAt = new Vector3()
  cameraUpdatePosition(new Vector3())
  adjustViewport()

  val scene = new Scene()
  scene.fog = new Fog(DrawingContext.bgColor.getHex(), cameraMax.y, cameraMax.y * 2)

  val ambient = new AmbientLight(0x606266)
  scene.add(ambient)

  val light = new PointLight(0xffffff, 1.0)
  light.position.set(5,10.0,5)
  scene.add(light)

  val light2 = new PointLight(0x113311, 1.0)
  light2.position.set(-10.0,-10.0,-3)
  scene.add(light2)

  val particleSystem = new ParticleSystem(this)

  val audio = new AudioContext(this)

  dom.window.onresize = (uiEv) => {
    adjustViewport()
  }

  def makeGfxConfig(): WebGLRendererParameters = {
    if (gfxDetail) {
      js.Dynamic.literal(
        antialias = true
      ).asInstanceOf[WebGLRendererParameters]
    } else {
      js.Dynamic.literal(
        antialias = false
      ).asInstanceOf[WebGLRendererParameters]
    }
  }

  def loadGfxDetail(): Boolean = {
    val txt = dom.window.localStorage.getItem(DrawingContext.QualityKey)
    gfxDetail = (txt != "low")
    gfxDetail
  }

  def setGfxDetail(highRes: Boolean) = {
    gfxDetail = highRes
    dom.window.localStorage.setItem(DrawingContext.QualityKey, if (highRes) "high" else "low")
  }

  def getGfxDetail(): Boolean = gfxDetail

  def adjustViewport() = {
    width = dom.window.innerWidth
    height = dom.window.innerHeight
    aspect = width / height
    renderer.asInstanceOf[js.Dynamic].setPixelRatio(devicePixelRatio)
    renderer.setSize(width, height)
    val minSide = Math.min(width, height)
    cameraSpace = if (minSide < 600) 2 + 14.0 * minSide / 600.0 else 16.0
    camera.aspect = aspect
    camera.updateProjectionMatrix()
  }

  def render(deltaTime: Double) = {
    ObjectShadow.updateAllShadows()
    audio.update(deltaTime)
    renderer.render(scene, camera)
  }

  def adjustCameraForMap(mapWidth: Int, mapHeight: Int): Unit = {
    val camSpace = cameraSpace
    if (mapWidth > camSpace || mapHeight > camSpace) {
      cameraMin.set(camSpace * .4, 0, camSpace * .4)
      cameraMax.set(mapWidth - camSpace * .4, 0, mapHeight - camSpace * .4)
    } else {
      cameraMin.set(mapWidth / 2, 0, mapHeight / 2)
      cameraMax.copy(cameraMin)
    }
  }

  def cameraUpdatePosition(lookAt: Vector3, spectatorOffSet: Double = 0.0): Unit = {
    val zOff = 2.0 + cameraSpace / 2.0
    cameraLookAt.copy(lookAt).clamp(cameraMin, cameraMax)
    val y = lookAt.y
    camera.position.copy(cameraLookAt).add(new Vector3((cameraSpace * .85) * Math.sin(.02 * y), 1.0 + cameraSpace + spectatorOffSet, zOff + spectatorOffSet))
    camera.lookAt(cameraLookAt.clone().add(new Vector3(0, .5 * y - spectatorOffSet * .5, zOff * (1 - Math.cos(.02 * y)))))
  }

  def cameraLookAt(lookAt: Vector3, spectatorOffSet: Double = 0.0): Unit = {
    val zOff = 2.0 + cameraSpace / 2.0
    cameraLookAt.copy(lookAt).clamp(cameraMin, cameraMax)
    val y = lookAt.y
    camera.position.copy(cameraLookAt).add(new Vector3((cameraSpace * .85) * Math.sin(.02 * y), 1.0 + y*.8 + cameraSpace + spectatorOffSet, zOff + spectatorOffSet))
    camera.lookAt(lookAt.clone())
  }
}