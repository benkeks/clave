package net.mrkeks.clave.game

object MonsterData {
  
  abstract sealed class State
  case class Idle() extends State
}

trait MonsterData
  extends PositionedObjectData {
  
  import MonsterData._
  
  var state: State = Idle()
}