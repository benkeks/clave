package net.mrkeks.clave.map

import org.denigma.threejs.Vector2
import org.denigma.threejs.Vector3

object MapData {
  object Tile extends Enumeration {
    val Empty, Wall, SolidWall, Something3, Something4,
        Something5, Something6, Something7, Monster, Player = Value
  }
  type Tile = Tile.Value
}

trait MapData {
  val width: Int
  val height: Int
  
  val topLeft = new Vector2(0, 0)
  val bottomRight = new Vector2(width - 1, height - 1)
  
  import MapData._
  
  protected val data = Array.ofDim[Tile](width, height)
  
  /** loads a map from a CSV string representation
   *  returns a list of special positions like starting positions of players or monsters */
  def loadFromString(strData: String) = {
    val rawArray = strData
      .split("\n")              // split by lines
      .map(_.split(","))        // split by columns
      .map(_.map(_.trim.toInt)) // convert to int
      
    var specialTiles = List[(Tile, (Int, Int))]()
      
    for (z <- 0 until height) {
      for (x <- 0 until width) {
        val tile = Tile(rawArray(z)(x))
        data(x)(z) = tile
        tile match {
          case Tile.Wall | Tile.Player | Tile.Monster =>
            specialTiles = (tile, (x,z)) :: specialTiles
            data(x)(z) = Tile.Empty
          case _ =>
        }
      }
    }
    
    specialTiles.groupBy(_._1).mapValues(_.map(_._2))
  }
  
  /** performs a spacially isolated sliding raycast on the tilemap
   *  dir should not be longer than 1.
   *  (sliding means that the component orthogonal to the collision normal
   *  is still allowed to take effect)
   *  (used for collision detection)
   *  (does not change src or dir) */
  def localSlideCast(src: Vector2, dir: Vector2): Vector2 = {
    val newSrc = new Vector2(src.x, src.y)
    
    newSrc.setX(src.x + dir.x)
    if (intersectsLevel(newSrc)) newSrc.setX(src.x)

    newSrc.setY(src.y + dir.y)
    if (intersectsLevel(newSrc)) newSrc.setY(src.y)
    
    newSrc.clamp(topLeft, bottomRight)
  }
  
  def vecToMapPos(v: Vector2) = {
    (v.x.round.toInt, v.y.round.toInt)
  }
  
  def vecToMapPos(v: Vector3) = {
    (v.x.round.toInt, v.z.round.toInt)
  }
  
  def intersectsLevel(v: Vector2): Boolean = {
    val (x, z) = vecToMapPos(v)
    intersectsLevel(x, z)
  }
  
  def intersectsLevel(x: Int, z: Int): Boolean = {
    if (x < 0 || x >= width || z < 0 || z >= height) {
      // being outside the level considered a 'collision'
      true
    } else {
      data(x)(z) match {
        case Tile.Wall | Tile.SolidWall =>
          true
        case _ =>
          false
      }
    }
  }
  
  def setData(x: Int, z: Int, newTile: Tile) = {
    data(x)(z) = newTile
  }
}