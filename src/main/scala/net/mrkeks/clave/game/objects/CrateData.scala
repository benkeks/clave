package net.mrkeks.clave.game.objects

import net.mrkeks.clave.game.characters.PlayerData
import net.mrkeks.clave.game.abstracts.FreezableObject
import net.mrkeks.clave.game.abstracts.PositionedObjectData

object CrateData {
  
  abstract sealed class State
  case class Standing() extends State
  case class Carried(by: PlayerData) extends State

  abstract sealed class Kind
  case object DefaultKind extends Kind
  case object PlayerLikeKind extends Kind
  case class FreezerKind(frozenMonster: Option[FreezableObject]) extends Kind
}

trait CrateData
  extends PositionedObjectData {
  
  import CrateData._
  
  var state: State = Standing()

  val kind: Kind
}