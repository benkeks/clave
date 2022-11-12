package net.mrkeks.clave.map

import org.denigma.threejs.Vector3
import scala.util.Random

object MapData {
  object Tile extends Enumeration {
    val Empty, Crate, SolidWall, GateClosing, GateOpen,
        GateClosed, Trigger, TriggerWithCrate, Monster, Player, Freezer = Value
  }
  type Tile = Tile.Value
  
  val notOnMap = (-1, -1)
}

trait MapData {
  val width: Int
  val height: Int
  
  val topLeft = new Vector3(0, 0, 0)
  val bottomRight = new Vector3(width - 1, 0, height - 1)
  
  import MapData._
  
  protected val data = Array.ofDim[Tile](width, height)

  /** To be implemented by class defining the dynamic level elements. */
  def isObstacleAt(xz: (Int, Int)): Boolean
  
  /** returns a list of special positions like starting positions of players or monsters */
  def loadFromArray(mapData: Array[Array[Int]]) = {
    var specialTiles = List[(Tile, (Int, Int))]()
    for (z <- 0 until height) {
      for (x <- 0 until width) {
        val tile = Tile(mapData(z)(x))
        tile match {
          case Tile.Crate | Tile.Player | Tile.Monster
             | Tile.GateOpen | Tile.GateClosed | Tile.Trigger | Tile.TriggerWithCrate | Tile.Freezer =>
            specialTiles = (tile, (x,z)) :: specialTiles
            data(x)(z) = Tile.Empty
          case _ =>
            data(x)(z) = tile
        }
      }
    }
    
    specialTiles.groupBy(_._1).view.mapValues(_.map(_._2))
  }
  
  /** performs a spacially isolated sliding raycast on the tilemap
   *  dir should not be longer than 1.
   *  (sliding means that the component orthogonal to the collision normal
   *  is still allowed to take effect)
   *  (used for collision detection)
   *  (does not change src or dir) */
  def localSlideCast(src: Vector3, dir: Vector3, bumpingDist: Double): Vector3 = {
    val newPos = src.clone()
    
    newPos.setX(src.x + dir.x + dir.x.sign * bumpingDist)
    newPos.setX(
      if (intersectsLevel(newPos, considerObstacles = true))
        src.x
      else
        src.x + dir.x
    )
    newPos.setZ(src.z + dir.z + dir.z.sign * bumpingDist)
    newPos.setZ(
      if (intersectsLevel(newPos, considerObstacles = true))
        src.z
      else
        src.z + dir.z
    )
    
    newPos.clamp(topLeft, bottomRight)
  }
  
  def vecToMapPos(v: Vector3) = {
   (v.x.round.toInt, v.z.round.toInt)
  }
  
  def mapPosToVec(xz: (Int, Int))= {
    new Vector3(xz._1, 0, xz._2)
  }
  
  def isOnMap(x: Int, z: Int) =
    x >= 0 && x < width && z >= 0 && z < height
    
  def isOnMapTupled = (isOnMap _).tupled
  
  def intersectsLevel(v: Vector3, considerObstacles: Boolean = false): Boolean = {
    val (x, z) = vecToMapPos(v)
    intersectsLevel(x, z, considerObstacles)
  }
  
  def intersectsLevel(xz: (Int, Int)): Boolean = {
    val (x, z) = xz
    intersectsLevel(x, z, false)
  }
  
  def intersectsLevel(x: Int, z: Int, considerObstacles: Boolean): Boolean = {
    if (x < 0 || x >= width || z < 0 || z >= height) {
      // being outside the level considered a 'collision'
      true
    } else {
      isTileBlocked(x, z) || (considerObstacles && isObstacleAt((x, z)))
    }
  }
  
  def isTile(x: Int, z: Int, tiles: Set[Tile]): Boolean = {
    if (isOnMap(x, z)) {
      tiles.contains(data(x)(z)) 
    } else {
      false
    }
  }
  
  def setData(x: Int, z: Int, newTile: Tile) = {
    data(x)(z) = newTile
  }
  
  /** searches squares around a position for a free field
   *  (if there are some in the same "distance", choose one randomly)
   *  (if everything fails, returns the input value) */
  def findNextFreeField(xz: (Int, Int)): (Int, Int) = {
    val (x, z) = xz
    var searchRadius = 1
    while(searchRadius < width) {
      val frontier = 
        // top
        (x - searchRadius + 1 until x + searchRadius).map((_, z - searchRadius)) ++
        // bottom
        (x - searchRadius + 1 until x + searchRadius).map((_, z + searchRadius)) ++
        // left
        (z - searchRadius + 1 until z + searchRadius).map((x - searchRadius, _)) ++
        // right
        (z - searchRadius + 1 until z + searchRadius).map((x + searchRadius, _))
      val freeFrontier = frontier.filterNot(intersectsLevel)
      if (freeFrontier.nonEmpty) {
        return freeFrontier(Random.nextInt(freeFrontier.size))
      }
      searchRadius += 1
    }
    xz
  }
  
  /** assumes that x,z is a valid field */
  protected def isTileBlocked(x: Int, z: Int) = {
    data(x)(z) match {
      case Tile.Crate | Tile.SolidWall | Tile.GateClosed | Tile.GateClosing =>
        true
      case _ =>
        false
    }
  }

  /** assumes that x,z is a valid field */
  def isTilePermanentlyBlocked(x: Int, z: Int) = {
    data(x)(z) match {
      case Tile.SolidWall | Tile.GateClosed =>
        true
      case _ =>
        false
    }
  }

}