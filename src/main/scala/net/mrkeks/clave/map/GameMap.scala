package net.mrkeks.clave.map

import scala.collection.mutable.MultiDict
import scala.scalajs.js
import scala.scalajs.js.Any.jsArrayOps
import org.denigma.threejs.BoxGeometry
import org.denigma.threejs.DataTexture
import org.denigma.threejs.Geometry
import org.denigma.threejs.Matrix4
import org.denigma.threejs.Mesh
import org.denigma.threejs.MeshFaceMaterial
import org.denigma.threejs.MeshLambertMaterial
import org.denigma.threejs
import org.denigma.threejs.PixelFormat
import org.denigma.threejs.TextureDataType
import org.denigma.threejs.TextureFilter
import net.mrkeks.clave.game.objects.Crate
import net.mrkeks.clave.game.Game
import net.mrkeks.clave.game.GameObject
import net.mrkeks.clave.game.characters.Monster
import net.mrkeks.clave.game.PositionedObject
import net.mrkeks.clave.view.DrawingContext
import org.denigma.threejs.Vector3
import net.mrkeks.clave.game.objects.Gate
import net.mrkeks.clave.game.objects.GateData
import scala.scalajs.js.typedarray.Uint32Array
import scala.scalajs.js.typedarray.Uint16Array
import org.denigma.threejs.PixelType
import org.denigma.threejs.Texture
import org.denigma.threejs.THREE
import org.denigma.threejs.PlaneGeometry
import org.denigma.threejs.MeshBasicMaterial

class GameMap(val width: Int, val height: Int)
  extends GameObject with MapData {
  
  import MapData._
  
  private val positionedObjects =
      MultiDict[(Int, Int), PositionedObject]()
  
  private object Materials {
    val wall = new MeshLambertMaterial()
    wall.color.setHex(0xdddd99)
    
    val solidWall = new MeshLambertMaterial()
    solidWall.color.setHex(0x888899)
    
    val flower = new MeshBasicMaterial()
    flower.transparent = true
    flower.depthWrite = false
    flower.polygonOffset = true
    flower.polygonOffsetUnits = -3
    DrawingContext.textureLoader.load("gfx/flowers.png", { tex: Texture =>
      flower.map = tex
      flower.needsUpdate = true
    })
  }
  
  private val materials = js.Array(
      Materials.wall,
      Materials.solidWall,
      Materials.flower)
  
  private val box = new BoxGeometry(1.0, 1.0, 1.0)
  box.faces.foreach { f => f.materialIndex = 0 }

  private val mesh = new Mesh(new Geometry(), materials.asInstanceOf[MeshFaceMaterial])
  
  private var victoryCheckNeeded = false
  protected val victoryCheck = Array.ofDim[Boolean](width, height)
  
  // underground presentation
  
  val groundShadow = new Uint16Array(width * height)
  (0 until width * height).foreach ( groundShadow.update(_, 255) )
  
  val groundShadowTexture = new DataTexture(groundShadow, width, height, threejs.THREE.RGBAFormat,
    threejs.THREE.UnsignedShort4444Type.asInstanceOf[TextureDataType], magFilter = threejs.THREE.LinearFilter)
  
  val groundMaterial = new MeshLambertMaterial()
  groundMaterial.color.setHex(0x808080)
  groundMaterial.map = groundShadowTexture
  
  DrawingContext.textureLoader.load("gfx/grass.png", { tex: Texture =>
    tex.repeat.set(1.2, 1.2)
    tex.wrapS = THREE.RepeatWrapping
    tex.wrapT = THREE.RepeatWrapping
    groundMaterial.lightMap = tex
    groundMaterial.needsUpdate = true
  })
  
  val undergroundPlane = new PlaneGeometry(1, 1)
  // duplicate uv coords in order for groundMaterial.lightMap to work
  undergroundPlane.faces.foreach { f => f.materialIndex = 0 }
  undergroundPlane.faceVertexUvs = js.Array(undergroundPlane.faceVertexUvs(0),
    undergroundPlane.faceVertexUvs(0).map(_.map(_.clone().multiplyScalar(2.0))))

  val underground = new Mesh(undergroundPlane, groundMaterial)
  underground.scale.set(width, height, 1.0)
  underground.rotation.x = -.5 * Math.PI
  underground.position.set(.5 * width - .5, -.5, .5 * height - .5)
  
  def init(context: DrawingContext): Unit = {
    for (x <- 0 until width) {
      for (z <- 0 until height) {
        updateLighting(x, z)
      }
    }
    
    context.scene.add(mesh)
    context.scene.add(underground)
  }
  
  def update(deltaTime: Double): Unit = {
    
  }
  
  def updateView(): Unit = {
    val beginUpdate = js.Date.now
    
    val rotateUp = new Matrix4().makeRotationX(-Math.PI * .5)
    
    val newGeometry = new Geometry()
    val drawingMatrix = new Matrix4()
    for (x <- 0 until width) {
      for (z <- 0 until height) {
        data(x)(z) match {
          case Tile.SolidWall =>
            drawingMatrix.makeTranslation(x, 0, z)
            newGeometry.merge(box, drawingMatrix, 1)
          case Tile.Empty =>
            if (Math.random() > .97) {
              // add occasional flowers
              drawingMatrix.makeTranslation(
                    x - .4 + .5 * Math.random(),
                    -.5, 
                    z - .4 + .5 * Math.random())
                    .multiply(rotateUp)
              val size = .8 + Math.random() * .7
              newGeometry.merge(new PlaneGeometry(size, size), drawingMatrix, 2)
            }
          case _ =>
        }
      }
    }
    mesh.geometry.dispose()
    mesh.geometry = newGeometry
  }
  
  def clear(context: DrawingContext): Unit = {
    mesh.geometry.dispose()
    groundShadowTexture.dispose()
    groundMaterial.dispose()
    context.scene.remove(mesh)
    context.scene.remove(underground)
  }
  
  def updateObjectPosition(o: PositionedObject, position: Vector3): (Int, Int) = {
    val newPosition = vecToMapPos(position)
    val oldPositionOnMap = o.getPositionOnMap
    
    oldPositionOnMap.foreach(positionedObjects.subtractOne(_ , o))
    positionedObjects.addOne(newPosition, o)
    
    o match {
      case _: Crate =>
        oldPositionOnMap.foreach(updateTile(_, Tile.Empty))
        updateTile(newPosition, Tile.Crate)
        victoryCheckNeeded = true
      case g: Gate =>
        val tile = g.state match {
          case _: GateData.Open => Tile.Empty
          case _: GateData.Closing => Tile.GateClosing
          case _: GateData.Closed => Tile.GateClosed
        }
        updateTile(newPosition, tile)
        victoryCheckNeeded = true
      case _ =>
        
    }
    
    newPosition
  }
  
  def isMonsterOn(xz: (Int, Int)): Boolean =
    getObjectsAt(xz).exists(_.isInstanceOf[Monster])
  
  def getObjectsAt(xz: (Int, Int)) = {
    positionedObjects.get(xz)
  }

  override def isObstacleAt(xz: (Int, Int)): Boolean = isMonsterOn(xz)
  
  def getAdjacentPositions(xz: (Int, Int)): List[(Int, Int)] = 
    getAdjacentPositions(xz._1, xz._2)
  
  def getAdjacentPositions(x: Int, z: Int): List[(Int, Int)] = {
    List((x-1, z), (x+1, z), (x, z-1), (x, z+1)).filter {
      case (x, z) => x >= 0 && x < width && z >= 0 && z < height
    }
  }
  
  def checkVictory(playerPositions: List[(Int, Int)]): Int = {
    if (victoryCheckNeeded && playerPositions.exists(xz => !intersectsLevel(xz))) {
      victoryCheckNeeded = false
      computeVictory(playerPositions)
    } else {
      -1
    }
  }
  
  /** computes whether there are no monsters reachable from a position
   *  if true returns how big the monster free region is. */
  private def computeVictory(playerPositions: List[(Int, Int)]): Int = {
    victoryCheck.foreach { row =>
      for (i <- 0 until height) {
        row(i) = false
      }
    }
    val discovered = collection.mutable.Stack().pushAll(playerPositions.filter(isOnMapTupled))
    var score = 0
    playerPositions.foreach {
      case (x, z) => if (isOnMap(x, z)) victoryCheck(x)(z) = true
    }
    
    while(discovered.nonEmpty) {
      val xz @ (x, z) = discovered.pop()
      data(x)(z) match {
        case Tile.Crate | Tile.SolidWall | Tile.GateClosed =>
          // stop recursion
        case _ if isMonsterOn(xz) =>
          // stop victory check
          return -1
        case _ =>
          score += 1
          getAdjacentPositions(x, z).foreach {
            case xz @ (x, z) =>
              if (!victoryCheck(x)(z)) {
                victoryCheck(x)(z) = true
                discovered.push(xz)
              }
          }
      }
    }
    return score
  }
  
  def updateTile(xz: (Int, Int), newTile: Tile) = xz match {
    case (x,z) =>
      if (isOnMap(x, z)) {
        setData(x, z, newTile)
        updateLighting(x, z)
        if (x < width - 1) updateLighting(x+1, z)
      }
  }
  
  def updateLighting(x: Int, z: Int, overlay: Int = 0): Unit = {
    groundShadow.update ( (x)+(height-z-1)*width,
      if (isTileBlocked(x, z))
        0x021f | overlay
      else if (x > 0 && isTileBlocked(x-1, z))
        0x1a3f | overlay 
      else
        0x5e4f | overlay
    )
    groundShadowTexture.needsUpdate = true
  }
  
  def updateAllLighting(): Unit = {
    for (x <- 0 until width) {
      for (z <- 0 until height) {
        updateLighting(x, z)
      }
    }
  }
  
  def victoryLighting(x: Int, z: Int): Unit = {
    if (isOnMap(x, z)) {
      updateLighting(x, z, if (victoryCheck(x)(z)) 0xee3f else 0x0000)
    }
  }
}