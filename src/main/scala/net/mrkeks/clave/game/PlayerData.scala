package net.mrkeks.clave.game

import org.denigma.threejs.Vector2

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
  }
  type Direction = Direction.Value
}

trait PlayerData extends GameObjectData {
  
  import PlayerData._
  
  val direction = new Vector2()
  
  var viewDirection = Direction.Down
}