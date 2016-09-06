package net.mrkeks.clave.map

import scala.collection.mutable.HashMap
import scala.collection.mutable.MultiMap
import scala.collection.mutable.Set
import scala.scalajs.js
import scala.scalajs.js.Any.jsArrayOps
import scala.scalajs.js.typedarray.Uint8Array

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

import net.mrkeks.clave.game.Crate
import net.mrkeks.clave.game.Game
import net.mrkeks.clave.game.GameObject
import net.mrkeks.clave.game.Monster
import net.mrkeks.clave.game.PositionedObject
import net.mrkeks.clave.view.DrawingContext


class GameMap(game: Game, val width: Int, val height: Int)
  extends GameObject with MapData {
  
  import MapData._
  
  private val positionedObjects: MultiMap[(Int, Int), PositionedObject] =
      new HashMap[(Int, Int), Set[PositionedObject]] 
        with MultiMap[(Int, Int), PositionedObject]
  
  private object Materials {
    val wall = new MeshLambertMaterial()
    wall.color.setHex(0xdddd99)
    
    val solidWall = new MeshLambertMaterial()
    solidWall.color.setHex(0x888899)
  }
  
  private val materials = new MeshFaceMaterial(js.Array(Materials.wall, Materials.solidWall))
  
  private val box = new BoxGeometry(1, 1, 1)
  box.faces.foreach { f => f.materialIndex = 0 }
  
  private val mesh = new Mesh(new Geometry(), materials)
  
  private var victoryCheckNeeded = false
  protected val victoryCheck = Array.ofDim[Boolean](width, height)
  
  val groundShadow = new Uint8Array(width * height)
  (0 until width * height).foreach ( groundShadow.update(_, 255) )
  
  val groundShadowTexture = new DataTexture()
  groundShadowTexture.format = threejs.THREE.LuminanceFormat
  groundShadowTexture.`type` = threejs.THREE.UnsignedByteType
  groundShadowTexture.magFilter = threejs.THREE.LinearFilter
  groundShadowTexture.image = {
    val w = width
    val h = height
    new js.Object {
      var width = w
      var height = h
      var data = groundShadow
    }
  }
  groundShadowTexture.needsUpdate = true
  
  val groundMaterial = new MeshLambertMaterial()
  groundMaterial.color.setHex(0x33dd22)
  groundMaterial.map = groundShadowTexture
  
  val underground = new Mesh(box, groundMaterial)
  underground.scale.set(width, 10, height)
  underground.position.set(.5 * width - .5, -5.5, .5 * height - .5)
  
//  private val shadows = ImageUtils.generateDataTexture(width, height, new Color(0x909090))
//  shadows.image.asInstanceOf[HTMLImageElement].
  
  def init(context: DrawingContext) {
    context.scene.add(mesh)
    context.scene.add(underground)
  }
  
  def update(deltaTime: Double) {
    if (victoryCheckNeeded) {
      checkVictory()
      victoryCheckNeeded = false
    }
  }
  
  def updateView() {
    val beginUpdate = js.Date.now
    
    val newGeometry = new Geometry()
    val drawingMatrix = new Matrix4()
    for (x <- 0 until width) {
      for (z <- 0 until height) {
        data(x)(z) match {
          case Tile.Wall =>
//            drawingMatrix.makeTranslation(x, 0, z)
//            newGeometry.merge(box, drawingMatrix.multiply(new Matrix4().makeRotationY(Math.random() * .1 - .05)), 0)
          case Tile.SolidWall =>
            drawingMatrix.makeTranslation(x, 0, z)
            newGeometry.merge(box, drawingMatrix, 1)
          case _ =>
        }
      }
    }
    mesh.geometry.dispose()
    mesh.geometry = newGeometry
    
    println("Map geometry update: " + (js.Date.now - beginUpdate) + "ms.")
  }
  
  def clear(context: DrawingContext) {
    mesh.geometry.dispose()
    groundShadowTexture.dispose()
    groundMaterial.dispose()
    context.scene.remove(mesh)
    context.scene.remove(underground)
  }
  
  def updateObjectPosition(o: PositionedObject) = {
    val newPosition = vecToMapPos(o.position)
    
    positionedObjects.removeBinding(o.positionOnMap, o)
    positionedObjects.addBinding(newPosition, o)
    
    if (o.isInstanceOf[Crate]) {
      updateTile(o.positionOnMap, Tile.Empty)
      updateTile(newPosition, Tile.Wall)
      victoryCheckNeeded = true
    } else if (o.isInstanceOf[Monster]) {
      updateTile(o.positionOnMap, Tile.Empty)
      updateTile(newPosition, Tile.Monster)
    }
    
    o.positionOnMap = newPosition
  }
  
  def getObjectsAt(xz: (Int, Int)) = {
    positionedObjects.getOrElse(xz, Set())
  }
  
  def getAdjacentPositions(x: Int, z: Int): List[(Int, Int)] = {
    List((x-1, z), (x+1, z), (x, z-1), (x, z+1)).filter {
      case (x, z) => x >= 0 && x < width && z >= 0 && z < height
    }
  }
  
  def checkVictory() {
    val score = computeVictory(game.getPlayerPositions)
    
    if (score >= 0) {
      game.notifyVictory(score)
    }
  }
  
  /** computes whether there are no monsters reachable from a position
   *  if true returns how big the monster free region is. */
  def computeVictory(playerPositions: List[(Int, Int)]): Int = {
    val checkMemory = victoryCheck.map(_.clone())
    val discovered = collection.mutable.Stack().pushAll(playerPositions)
    var score = 0
    playerPositions.foreach {
      case (x, z) => checkMemory(x)(z) = true
    }
    
    while(discovered.nonEmpty) {
      val (x, z) = discovered.pop()
      data(x)(z) match {
        case Tile.Monster =>
          return -1
        case Tile.Wall | Tile.SolidWall =>
          // nothing
        case _ =>
          score += 1
          getAdjacentPositions(x, z).foreach {
            case xz @ (x, z) =>
              if (!checkMemory(x)(z)) {
                checkMemory(x)(z) = true
                discovered.push(xz)
              }
          }
      }
    }
    return score
  }
  
  def updateTile(xz: (Int, Int), newTile: Tile) = xz match {
    case (x,z) =>
      if (x >= 0 && x < width && z >= 0 && z < height) {
        setData(x, z, newTile)
        updateLighting(x, z)
        if (x < width - 1) updateLighting(x+1, z)
//        if (z < height - 1) updateLighting(x, z+1)
//        if (x < width - 1 && z < height - 1) updateLighting(x+1, z+1)
      }
  }
  
  def updateLighting(x: Int, z: Int) = {
    groundShadow.update ( (x)+(height-z-1)*width,
      if (data(x)(z) == Tile.Wall)
        0x22
      else if (x > 0 && data(x-1)(z) == Tile.Wall)
//          || (z > 0 && data(x)(z-1) == Tile.Wall)
//          || (z > 0 && x > 0 && data(x-1)(z-1) == Tile.Wall)
        0xbb
      else
        0xFF
    )
    groundShadowTexture.needsUpdate = true
  }
}