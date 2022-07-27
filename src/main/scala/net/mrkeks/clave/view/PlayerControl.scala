package net.mrkeks.clave.view

import net.mrkeks.clave.game.characters.Player

class PlayerControl(val player: Player, val input: Input) {
  
  input.keyPressListener.addOne(PlayerControl.ActionCharStr, actionKey _)
  
  def update(deltaTime: Double): Unit = {
    player.direction.set(0,0,0)

    if (input.keysDown(PlayerControl.LeftCode) || input.gamepad.exists(_.axes(0) <= -.5)) player.direction.x -= 1
    if (input.keysDown(PlayerControl.RightCode) || input.gamepad.exists(_.axes(0) >= .5)) player.direction.x += 1
    if (input.keysDown(PlayerControl.UpCode) || input.gamepad.exists(_.axes(1) <= -.5)) player.direction.z -= 1
    if (input.keysDown(PlayerControl.DownCode) || input.gamepad.exists(_.axes(1) >= .5)) player.direction.z += 1

  }
  
  def actionKey(): Unit = {
    player.doAction()
  }
  
  def clear(): Unit = {
    input.keyPressListener.subtractOne(PlayerControl.ActionCharStr, actionKey _)
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