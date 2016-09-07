package net.mrkeks.clave.game

import org.denigma.threejs.Vector2
import org.denigma.threejs.Vector3

object PlayerData extends PositionedObjectData {
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
  import PositionedObjectData.Direction
  
  val direction = new Vector2()
  
  var viewDirection = Direction.Down
  
  var state: State = Idle()
}