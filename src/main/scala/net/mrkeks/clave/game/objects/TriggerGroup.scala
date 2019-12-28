package net.mrkeks.clave.game.objects

import net.mrkeks.clave.game.GameObject
import net.mrkeks.clave.view.DrawingContext

/** A group of triggers that together opens a group of gates. */
class TriggerGroup extends GameObject {
  
  val triggers = collection.mutable.Stack[TriggerData]()
  
  val gates = collection.mutable.Stack[Gate]()
  
  def addTrigger(trigger: TriggerData) = {
    triggers.push(trigger)
  }
  
  def addGate(gate: Gate) = {
    gates.push(gate)
  }
  
  def init(context: DrawingContext): Unit = {
    // trigger groups have no own visual representation
  }
  
  def update(deltaTime: Double): Unit = {
    if (triggers.forall(_.state.isInstanceOf[TriggerData.Pushed])) {
      gates.foreach(_.close())
    } else {
      gates.foreach(_.open())
    }
  }
  
  def clear(context: DrawingContext): Unit = {
    gates.clear()
    triggers.clear()
  }
}