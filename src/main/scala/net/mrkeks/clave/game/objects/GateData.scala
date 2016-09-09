package net.mrkeks.clave.game.objects

import net.mrkeks.clave.game.PositionedObjectData

object GateData {
  
  abstract sealed class State
  case class Open() extends State
  case class Closed() extends State
  case class Closing() extends State

}

trait GateData
  extends PositionedObjectData {
  
  import GateData._
  
  var state: State = Open()
}
