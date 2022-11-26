package net.mrkeks.clave.game.characters

import org.denigma.threejs.Vector3
import net.mrkeks.clave.game.abstracts.PositionedObjectData
import net.mrkeks.clave.game.objects.CrateData

object MonsterData {
  
  abstract sealed class State
  case class Idle(var strollCoolDown: Double = 3000.0) extends State
  case class MoveTo(tar: Vector3) extends State
  case class ChargeJumpTo(tar: Vector3, var progress: Double = 0.0) extends State
  case class JumpTo(tar: Vector3, from: Vector3, var ySpeed: Double = 0.0) extends State
  case class PushedTo(tar: Vector3, var ySpeed: Double = 0.0, forceful: Boolean = false) extends State
  case class MergingWith(otherMonster: MonsterData, var progress: Double = 0.0) extends State
  case class Frozen(byCrate: CrateData) extends State
  case class Paralyzed(var coolDown: Double = 3000) extends State

  abstract sealed class MonsterKind
  case object AggressiveMonster extends MonsterKind
  case object FrightenedMonster extends MonsterKind
}

trait MonsterData
  extends PositionedObjectData {

  import MonsterData._

  var state: State = Idle()

  var sizeLevel: Int = 1

  val kind: MonsterKind
}