package net.mrkeks.clave.view

import net.mrkeks.clave.game.characters.Player

class PlayerControl(var player: Player, val input: Input) extends Input.ActionKeyListener {

  private var actionRegistered = false
  var hasEverMoved = false
  var hasEverActed = false

  input.actionKeyListeners.addOne(this)

  def update(deltaTime: Double): Unit = {
    player.direction.set(input.arrowDirectionX, 0, input.arrowDirectionY)

    hasEverMoved ||= player.direction.x != 0 || player.direction.z != 0

    if (actionRegistered) {
      hasEverActed |= player.doAction()
      actionRegistered = false
    }
  }

  def resetState(): Unit = {
    player.direction.set(0,0,0)
    actionRegistered = false
  }

  def handleActionKey(): Unit = {
    actionRegistered = true
  }
  
  def clear(): Unit = {
    input.actionKeyListeners.removeAll(_ == this)
  }

  def reassign(newPlayer: Player) = {
    player = newPlayer
  }
}

object PlayerControl {
  val LeftCode = 37
  val RightCode = 39
  val UpCode = 38
  val DownCode = 40
  
  val ActionCode = 32
  val ActionChar = ' '
  val ActionCharStr = ActionChar.toString

}