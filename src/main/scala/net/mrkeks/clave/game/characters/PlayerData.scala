package net.mrkeks.clave.game.characters

import org.denigma.threejs.Vector3
import net.mrkeks.clave.game.objects.CrateData
import net.mrkeks.clave.game.abstracts.GameObjectData
import net.mrkeks.clave.game.abstracts.PositionedObjectData

object PlayerData extends PositionedObjectData {
  abstract sealed class State {
    /** player speed of movement in a certain state */
    val speed = .007
  }
  case class Idle() extends State
  case class Carrying(crate: CrateData) extends State {
    override val speed = .0045
  }
  case class Dead() extends State {
    override val speed = 0.0
    var deathAnim = 1.0
  }

  case class Poisoned() extends State {
    override val speed = 0.0
    var deathAnim = 1.0
  }

  case class Frozen(byCrate: CrateData) extends State {
    override val speed = 0.0
  }

  case class Spawning(var ySpeed: Double = 0) extends State
}

trait PlayerData extends GameObjectData with PositionedObjectData {

  import PlayerData._

  val direction = new Vector3()

  var state: State = Idle()

  var size: Int = 1

  def isAlive() = {
    !state.isInstanceOf[Dead] &&
    !state.isInstanceOf[Frozen] &&
    !state.isInstanceOf[Poisoned]
  }
}