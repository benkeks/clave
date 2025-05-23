package net.mrkeks.clave.game.objects

import net.mrkeks.clave.game.abstracts.PositionedObjectData

object TriggerData {
  
  abstract sealed class State
  case class Idle() extends State
  case class Pushed(by: PositionedObjectData) extends State
}

trait TriggerData
  extends PositionedObjectData {
  
  import TriggerData._
  
  var state: State = Idle()
  
  //val group = defaultTriggerGroup
  //defaultTriggerGroup.addTrigger(this)
}