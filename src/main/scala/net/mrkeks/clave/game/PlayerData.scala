package net.mrkeks.clave.game

import org.denigma.threejs.Vector2
import org.denigma.threejs.Vector3
import net.mrkeks.clave.game.objects.CrateData

object PlayerData extends PositionedObjectData {
  abstract sealed class State {
    /** player speed of movement in a certain state */
    val speed = .008
  }
  case class Idle() extends State
  case class Carrying(crate: CrateData) extends State {
    override val speed = .005
  }
  case class Dead() extends State {
    override val speed = 0.0
  }
}

trait PlayerData extends GameObjectData with PositionedObjectData {
  
  import PlayerData._
  import PositionedObjectData.Direction
  
  val direction = new Vector2()
  
  var viewDirection = Direction.Down
  
  var state: State = Idle()
}