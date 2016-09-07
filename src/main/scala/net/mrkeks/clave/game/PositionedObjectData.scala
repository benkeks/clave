package net.mrkeks.clave.game

import net.mrkeks.clave.map.GameMap
import org.denigma.threejs.Vector3
import org.denigma.threejs.Vector2
import net.mrkeks.clave.util.markovIf
import net.mrkeks.clave.map.MapData

object PositionedObjectData {
  object Direction extends Enumeration {
    val Up, Down, Left, Right = Value
    
    /** converts vector to four-way direction
     *  prioritizes up-down movement; defaults to Down */
    def fromVec(v: Vector2) = {
      if (v.y < 0) {
        Up 
      } else if (v.y == 0 && v.x > 0) {
        Right
      } else if (v.y == 0 && v.x < 0) {
        Left
       } else {
        Down
      }
    }
    
    def toVec(d: Direction.Value) = d match {
      case Up    => new Vector2( 0,-1)
      case Down  => new Vector2( 0, 1)
      case Left  => new Vector2(-1, 0)
      case Right => new Vector2( 1, 0)
    }
    
    def toVec3(d: Direction.Value) = d match {
      case Up    => new Vector3( 0, 0,-1)
      case Down  => new Vector3( 0, 0, 1)
      case Left  => new Vector3(-1, 0, 0)
      case Right => new Vector3( 1, 0, 0)
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
}