package net.mrkeks.clave.game.abstracts

import net.mrkeks.clave.map.GameMap
import org.denigma.threejs.Vector3
import net.mrkeks.clave.util.markovIf
import net.mrkeks.clave.map.MapData

object PositionedObjectData {
  object Direction extends Enumeration {
    val Up, Down, Left, Right = Value

    /** converts vector to four-way direction (in x-z-plane)
     *  prioritizes up-down movement; defaults to Down */
    def fromVec(v: Vector3) = {
      if (v.z < 0) {
        Up 
      } else if (v.z == 0 && v.x > 0) {
        Right
      } else if (v.z == 0 && v.x < 0) {
        Left
       } else {
        Down
      }
    }

    def toVec(d: Direction.Value) = d match {
      case Up    => new Vector3( 0, 0,-1)
      case Down  => new Vector3( 0, 0, 1)
      case Left  => new Vector3(-1, 0, 0)
      case Right => new Vector3( 1, 0, 0)
    }

    def toRadians(d: Direction.Value) = d match {
      case Up    => 0
      case Down  => Math.PI
      case Left  => Math.PI * .5
      case Right => Math.PI * 1.5
    }

    def fromRadians(radianDirection: Double) = {
      val dir = radianDirection % (2 * Math.PI)
      if (dir >= 1.75 * Math.PI || dir < .25 * Math.PI) {
        Up
      } else if (dir < .75 * Math.PI) {
        Left
      } else if (dir < 1.25 * Math.PI) {
        Down
      } else {
        Right
      }
    }
//    
//    def fromRnd(rnd: Double) = {
//      if (rnd < .25) {
//        Up
//      } else if (rnd < .5) {
//        Down
//      } else if (rnd < .75) {
//        Left
//      } else {
//        Right
//      }
//    }
    
    def randomDirection() = {
      //fromRnd(Math.random())
      markovIf (.25) {
        Up
      }.markovElseIf (.25) {
        Down
      }.markovElseIf (.25) {
        Left
      } markovElse {
        Right
      }
    }
  }
  type Direction = Direction.Value

  val OffMapPosition = new Vector3(MapData.notOnMap._1, -1, MapData.notOnMap._2)
}

trait PositionedObjectData  {
  
  protected var positionOnMap = MapData.notOnMap
  
  protected val position = new Vector3()
  
  /** returns a copy of the internal position information */
  def getPosition = position.clone()
  
  /** returns the position on the map if the entity is placed on the map */
  def getPositionOnMap = {
    if (positionOnMap._1 >= 0 && positionOnMap._2 >= 0) {
      Some(positionOnMap)
    } else {
      None
    }
  }

  import PositionedObjectData.Direction

  var viewDirection = Direction.Down
}