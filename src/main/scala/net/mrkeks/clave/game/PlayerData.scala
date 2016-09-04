package net.mrkeks.clave.game

import org.denigma.threejs.Vector2
import org.denigma.threejs.Vector3

object PlayerData {
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
  }
  type Direction = Direction.Value
  
  abstract sealed class State {
    val speed = .01
  }
  case class Idle() extends State
  case class Carrying(crate: CrateData) extends State {
    override val speed = .005
  }
}

trait PlayerData extends GameObjectData with PositionedObjectData {
  
  import PlayerData._
  
  val direction = new Vector2()
  
  var viewDirection = Direction.Down
  
  var state: State = Idle()
}