package net.mrkeks.clave.game

object CrateData {
  
  abstract sealed class State
  case class Standing() extends State
  case class Carried(by: PlayerData) extends State
}

trait CrateData
  extends PositionedObjectData {
  
  import CrateData._
  
  var state: State = Standing()
}