package net.mrkeks.clave.map

import net.mrkeks.clave.game.Game
import net.mrkeks.clave.game.abstracts.GameObject
import net.mrkeks.clave.game.abstracts.PositionedObject
import net.mrkeks.clave.game.characters.Player
import net.mrkeks.clave.game.characters.{Monster, MonsterData}
import net.mrkeks.clave.game.objects.Crate
import net.mrkeks.clave.game.objects.CrateData.FreezerKind
import net.mrkeks.clave.game.objects.Gate
import net.mrkeks.clave.game.objects.GateData
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.util.Mathf

import scala.scalajs.js
import scala.scalajs.js.Any.jsArrayOps
import scala.scalajs.js.typedarray.Uint32Array
import scala.scalajs.js.typedarray.Uint16Array
import scala.collection.mutable.MultiDict

import org.denigma.threejs
import org.denigma.threejs.BoxGeometry
import org.denigma.threejs.DataTexture
import org.denigma.threejs.BufferGeometry
import org.denigma.threejs.Matrix4
import org.denigma.threejs.Mesh
import org.denigma.threejs.MeshLambertMaterial
import org.denigma.threejs.PixelFormat
import org.denigma.threejs.TextureDataType
import org.denigma.threejs.TextureFilter
import org.denigma.threejs.Vector3
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
    
    val grassPatch = new MeshBasicMaterial()
    grassPatch.transparent = true
    grassPatch.depthWrite = false
    grassPatch.polygonOffset = true
    grassPatch.polygonOffsetUnits = -.01
    grassPatch.opacity = .8
    DrawingContext.textureLoader.load("gfx/flowers_white.png", { tex: Texture =>
      grassPatch.map = tex
      grassPatch.needsUpdate = true
    })
  }

  private val box = new BoxGeometry(1.0, 1.0, 1.0)
  box.groups.foreach { g => g.materialIndex = 0 }

  private val MaxWallCount: Int = 1024
  private val walls = new threejs.InstancedMesh(box, Materials.solidWall, MaxWallCount)

  private val MaxGrassCount: Int = 1024
  private val grass = new threejs.InstancedMesh(new PlaneGeometry(1,1), Materials.grassPatch, MaxGrassCount)
  private val positionedGroundItems =
      MultiDict[(Int, Int), Int]()

  val center = new Vector3(width / 2.0, 0, height / 2.0)

  protected var playerDangerousness = Array.ofDim[Int](width, height)
  private var playerDangerousnessUpdate = Array.ofDim[Int](width, height)
  private var playerDangerousnessUpdateLine: Int = 0

  private var victoryCheckNeeded = true
  protected val victoryCheck = Array.ofDim[Int](width, height)
  var enableVictoryLighting: Double = 0

  // underground presentation
  val groundShadow = new Uint16Array(width * height)
  (0 until width * height).foreach ( groundShadow.update(_, 255) )
  
  val groundShadowTexture = new DataTexture(groundShadow, width, height, threejs.THREE.RGBAFormat,
    threejs.THREE.UnsignedShort4444Type.asInstanceOf[TextureDataType], magFilter = threejs.THREE.LinearFilter)
  
  val groundMaterial = new MeshLambertMaterial()
  groundMaterial.color.setHex(0xf0f0f0)
  groundMaterial.map = groundShadowTexture
  groundMaterial.reflectivity = .5

  val underground = new Mesh(new PlaneGeometry(1, 1), groundMaterial)
  underground.scale.set(width, height, 1.0)
  underground.rotation.x = -.5 * Math.PI
  underground.position.set(.5 * width - .5, -.5, .5 * height - .5)

  val undergroundEdges = new Mesh(makeUndergroundEdges(), groundMaterial)
  undergroundEdges.position.set(-.5, -.5, -.5)

  val wind = new Vector3(.001,0,0)
  val grassDrift = new threejs.Vector2(0,0)

  def init(context: DrawingContext): Unit = {
    context.scene.add(walls)
    context.scene.add(grass)
    context.scene.add(underground)
    context.scene.add(undergroundEdges)
    updateAllLighting()
  }
  
  def update(deltaTime: Double): Unit = {

    // wind on flowers
    if (Materials.grassPatch.map != null) {
      grassDrift.x = grassDrift.x + wind.x * deltaTime
      Materials.grassPatch.map.offset.x = Math.sin(grassDrift.x)*.03
      Materials.grassPatch.map.offset.y = Math.sin(grassDrift.x+2)*.01
      Materials.grassPatch.map.repeat.y = 1.1 + Math.sin(grassDrift.x * 1.1 + 1)*.05
      Materials.grassPatch.map.repeat.x = 1.1
    }

    // update the player dangerousness in scan lines every 100 ms
    for (i <- 0 until (height * deltaTime / 100).ceil.toInt) {
      for (x <- 0 until width) {
        val pos = (x, playerDangerousnessUpdateLine)
        playerDangerousnessUpdate(x)(playerDangerousnessUpdateLine) = data(x)(playerDangerousnessUpdateLine) match {
          case Tile.Crate | Tile.SolidWall | Tile.GateClosed =>
            20
          case _ if getObjectsAt(pos).exists(_.isInstanceOf[Player]) =>
            60
          case _ =>
            val adjacent = getAdjacentPositions(pos) :+ pos
            adjacent.map {
              case (x0,z0) => playerDangerousness(x0)(z0)
            }.sum / adjacent.length + (if (x == 0 || x == width - 1 || playerDangerousnessUpdateLine == 0 || playerDangerousnessUpdateLine == height - 1) 3 else -1)
          }
      }
      playerDangerousnessUpdateLine += 1
      if (playerDangerousnessUpdateLine >= height) {
        playerDangerousnessUpdateLine = 0
        val flip = playerDangerousness
        playerDangerousness = playerDangerousnessUpdate
        playerDangerousnessUpdate = flip
      }
    }

  }
  
  def updateView(): Unit = {
    val rotateUp = new Matrix4().makeRotationX(-Math.PI * .5)
    val rotationMatrix = new Matrix4()
    val drawingMatrix = new Matrix4()
    var wallCount = 0
    var grassCount = 0
    positionedGroundItems.clear()
    for (x <- 0 until width) {
      for (z <- 0 until height) {
        data(x)(z) match {
          case Tile.SolidWall =>
            rotationMatrix.makeRotationX(.1 * Math.random() - .05)
            drawingMatrix.makeTranslation(x, -.1 + .1 * Math.random(), z).multiply(rotationMatrix).scale(new Vector3(1,Math.random()*.2+1.2,1))
            walls.setMatrixAt(wallCount, drawingMatrix)
            wallCount += 1
          case Tile.Empty if positionedObjects.get((x,z)).isEmpty =>
            if (Math.random() > .7) {
              rotationMatrix.makeRotationY(2 * Math.PI * Math.random()).multiply(rotateUp)
              drawingMatrix.makeTranslation(x, -.49, z).multiply(rotationMatrix).scale(new Vector3(Math.random()*.3+1.0,1,Math.random()*.3+1.0))
              grass.setMatrixAt(grassCount, drawingMatrix)
              positionedGroundItems.addOne((x,z), grassCount)
              grassCount += 1
            }
          case _ =>
        }
      }
    }
    walls.count = wallCount
    grass.count = grassCount
    walls.instanceMatrix.needsUpdate = true
    grass.instanceMatrix.needsUpdate = true
  }

  def makeUndergroundEdges(): BufferGeometry = {
    val edgeGeometry = new BufferGeometry()
    val vertices = new js.typedarray.Float32Array(6 * (2 * width + 2 * height + 1))
    for (x <- 0 to width) {
      vertices(x * 6 + 0) = x.toFloat
      vertices(x * 6 + 1) = 0.toFloat
      vertices(x * 6 + 2) = (height + Math.random()).toFloat
      vertices(x * 6 + 3) = x.toFloat
      vertices(x * 6 + 4) = -5f
      vertices(x * 6 + 5) = height.toFloat
    }
    val triangles = new js.typedarray.Uint16Array(6 * (2 * width + 2 * height))
    for (x <- 0 until width) {
      triangles(x * 6 + 0) = x
      triangles(x * 6 + 1) = x + 1
      triangles(x * 6 + 2) = x + 2
      triangles(x * 6 + 3) = x + 2
      triangles(x * 6 + 4) = x + 1
      triangles(x * 6 + 5) = x + 3
    }
    edgeGeometry.setAttribute("position", new threejs.Float32BufferAttribute(vertices, 3))
    edgeGeometry.setIndex(new threejs.Uint16BufferAttribute(triangles, 1))
    edgeGeometry.setDrawRange(0, 3 * 2 * width)
    edgeGeometry
  }

  def clear(context: DrawingContext): Unit = {
    walls.geometry.dispose()
    groundShadowTexture.dispose()
    groundMaterial.dispose()
    undergroundEdges.geometry.dispose()
    context.scene.remove(walls)
    context.scene.remove(grass)
    context.scene.remove(underground)
    context.scene.remove(undergroundEdges)
  }
  
  def updateObjectPosition(o: PositionedObject, position: Vector3): (Int, Int) = {
    val newPosition = vecToMapPos(position)
    val oldPositionOnMap = o.getPositionOnMap
    
    oldPositionOnMap.foreach(positionedObjects.subtractOne(_ , o))
    positionedObjects.addOne(newPosition, o)
    
    o match {
      case c: Crate =>
        oldPositionOnMap.foreach(updateTile(_, Tile.Empty))
        c.kind match {
          case FreezerKind(None) => updateTile(newPosition, Tile.Freezer)
          case _ => updateTile(newPosition, Tile.Crate)
        }
        victoryCheckNeeded = true
      case g: Gate =>
        val tile = g.state match {
          case _: GateData.Open => Tile.Empty
          case _: GateData.Closing => Tile.GateClosing
          case _: GateData.Closed => Tile.GateClosed
        }
        updateTile(newPosition, tile)
        victoryCheckNeeded = true
      case m: Monster if (m.state.isInstanceOf[MonsterData.Frozen]) =>
        victoryCheckNeeded = true
      case _ =>
        
    }
    
    newPosition
  }
  
  def isMonsterOn(xz: (Int, Int)): Boolean =
    getObjectsAt(xz).exists { case m: Monster => m.kind != MonsterData.FriendlyMonster; case _ => false }
  
  def getObjectsAt(xz: (Int, Int)): Set[PositionedObject] = {
    if (xz == MapData.notOnMap) Set() else positionedObjects.get(xz).toSet
  }

  override def isObstacleAt(xz: (Int, Int)): Boolean = getObjectsAt(xz).exists(o => o.isInstanceOf[Monster])

  def getAdjacentPositions(xz: (Int, Int)): List[(Int, Int)] = 
    getAdjacentPositions(xz._1, xz._2)
  
  def getAdjacentPositions(x: Int, z: Int): List[(Int, Int)] = {
    List((x-1, z), (x+1, z), (x, z-1), (x, z+1)).filter {
      case (x, z) => x >= 0 && x < width && z >= 0 && z < height
    }
  }
  
  def getPlayerDangerousness(xz: (Int, Int)): Int = xz match {
    case (x, z) if x >= 0 && x < width && z >= 0 && z < height => playerDangerousness(x)(z)
    case _ => 0
  }

  def checkVictory(playerPositions: List[(Int, Int)]): List[(Int, Int)] = {
    if (victoryCheckNeeded && playerPositions.exists(xz => !intersectsLevel(xz))) {
      victoryCheckNeeded = false
      computeVictory(playerPositions)
    } else {
      List()
    }
  }
  
  /** computes whether there are no monsters reachable from a position
   *  if true returns a list representing the monster free region. */
  private def computeVictory(playerPositions: List[(Int, Int)]): List[(Int, Int)] = {
    val Infinity = -1
    victoryCheck.foreach { row =>
      for (i <- 0 until height) {
        row(i) = Infinity
      }
    }
    val todo = collection.mutable.Queue().appendAll(playerPositions.filter(isOnMapTupled))
    var score = 0
    playerPositions.foreach {
      case (x, z) => if (isOnMap(x, z)) victoryCheck(x)(z) = 0
    }
    while(todo.nonEmpty) {
      val xz @ (x, z) = todo.dequeue()
      score += 1
      getAdjacentPositions(x, z).foreach {
        case x1z1 @ (x1, z1) =>
          data(x1)(z1) match {
            case _ if isMonsterOn(x1z1) =>
              // stop victory check
              return List()
            case Tile.Crate | Tile.SolidWall | Tile.GateClosed =>
              // stop recursion
            case _ =>
              if (victoryCheck(x1)(z1) == Infinity) {
                victoryCheck(x1)(z1) = victoryCheck(x)(z) + 1
                todo.append(x1z1)
              }
          }
      }
    }
    // it only happens once per level that we actually receive a winning region. so only then restore the order of discovered fields:
    val victoryRegion = for {
      z <- 0 until height
      x <- 0 until width
      if victoryCheck(x)(z) != Infinity
    } yield (x,z)
    return victoryRegion.toList.sortBy { case (x,z) => victoryCheck(x)(z) }
  }
  
  def updateTile(xz: (Int, Int), newTile: Tile) = xz match {
    case (x,z) =>
      if (isOnMap(x, z)) {
        setData(x, z, newTile)
        updateLighting(x, z)
        if (x < width - 1) updateLighting(x+1, z)
      }
  }
  
  def updateLighting(x: Int, z: Int): Unit = {
    val color = if (isTileBlocked(x, z))
        0x021f
      else if (x > 0 && isTileBlocked(x-1, z))
        0x193f
      else
        0x5b4f
    val overlay = if (victoryCheck(x)(z) >= 0 && victoryCheck(x)(z) < enableVictoryLighting) 0xee7f else 0x0000
    groundShadow.update((x)+(height-z-1)*width, color | overlay)
    val objColor = new threejs.Color( if (x > 0 && isTileBlocked(x-1, z)) 0x999999ff else 0xffffffff)
    positionedGroundItems.get((x,z)).foreach(i => grass.setColorAt(i, objColor))
    if (grass.instanceColor != null) grass.instanceColor.needsUpdate = true
    groundShadowTexture.needsUpdate = true
  }
  
  def updateAllLighting(): Unit = {
    for (x <- 0 until width) {
      for (z <- 0 until height) {
        updateLighting(x, z)
      }
    }
  }
  
  def victoryLighting(x: Int, z: Int): Int = {
    if (isOnMap(x, z)) {
      val v = victoryCheck(x)(z)
      updateLighting(x, z)
      return v
    } else {
      return 0
    }
  }
}