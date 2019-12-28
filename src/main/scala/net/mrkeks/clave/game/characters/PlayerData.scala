package net.mrkeks.clave.game.characters

import org.denigma.threejs.Vector3
import net.mrkeks.clave.game.objects.CrateData
import net.mrkeks.clave.game.GameObjectData
import net.mrkeks.clave.game.PositionedObjectData

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
    var deathAnim = 1.0
  }

  case class Spawning(var ySpeed: Double = 0) extends State
}

trait PlayerData extends GameObjectData with PositionedObjectData {
  
  import PlayerData._
  import PositionedObjectData.Direction
  
  val direction = new Vector3()
  
  var viewDirection = Direction.Down
  
  var state: State = Idle()
  
  def isAlive() = {
    !state.isInstanceOf[Dead]
  }
}