package net.mrkeks.clave.view

import net.mrkeks.clave.game.characters.Player

class PlayerControl(val player: Player, val input: Input) {
  
  input.keyPressListener.addOne(PlayerControl.ActionCharStr, (this, actionKey))

  private var actionRegistered = false
  
  def update(deltaTime: Double): Unit = {
    player.direction.set(0,0,0)

    if (input.keysDown(PlayerControl.LeftCode) || input.gamepad.exists(_.axes(0) <= -.5)) player.direction.x -= 1
    if (input.keysDown(PlayerControl.RightCode) || input.gamepad.exists(_.axes(0) >= .5)) player.direction.x += 1
    if (input.keysDown(PlayerControl.UpCode) || input.gamepad.exists(_.axes(1) <= -.5)) player.direction.z -= 1
    if (input.keysDown(PlayerControl.DownCode) || input.gamepad.exists(_.axes(1) >= .5)) player.direction.z += 1

    if (actionRegistered) {
      player.doAction()
      actionRegistered = false
    }
  }

  def resetState(): Unit = {
    player.direction.set(0,0,0)
    actionRegistered = false
  }

  def actionKey(): Unit = {
    actionRegistered = true
  }
  
  def clear(): Unit = {
    input.keyPressListener.removeKey(PlayerControl.ActionCharStr)
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