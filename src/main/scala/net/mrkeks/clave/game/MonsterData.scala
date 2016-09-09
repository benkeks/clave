package net.mrkeks.clave.game

import org.denigma.threejs.Vector3

object MonsterData {
  
  abstract sealed class State
  case class Idle(var strollCoolDown: Double = 2000.0) extends State
  case class MoveTo(tar: Vector3) extends State
  case class ChargeJumpTo(tar: Vector3, var progress: Double = 0.0) extends State
  case class JumpTo(tar: Vector3, from: Vector3, var ySpeed: Double = 0.0) extends State
  case class PushedTo(tar: Vector3, var ySpeed: Double = 0.0) extends State
}

trait MonsterData
  extends PositionedObjectData {
  
  import MonsterData._
  
  var state: State = Idle()
}