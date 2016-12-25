package net.mrkeks.clave.game.objects

import net.mrkeks.clave.game.characters.PlayerData
import net.mrkeks.clave.game.PositionedObjectData

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