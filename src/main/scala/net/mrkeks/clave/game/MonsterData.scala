package net.mrkeks.clave.game

import org.denigma.threejs.Vector3

object MonsterData {
  
  abstract sealed class State
  case class Idle() extends State
  case class MoveTo(tar: Vector3) extends State
}

trait MonsterData
  extends PositionedObjectData {
  
  import MonsterData._
  
  var state: State = Idle()
}