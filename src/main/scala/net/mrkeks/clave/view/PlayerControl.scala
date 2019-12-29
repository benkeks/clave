package net.mrkeks.clave.view

import net.mrkeks.clave.game.characters.Player

class PlayerControl(val player: Player, val input: Input) {
  
  input.keyPressListener.addOne(" ", actionKey _)
  
  def update(deltaTime: Double): Unit = {
    player.direction.set(0,0,0)
    
    if (input.keysDown(37)) player.direction.x -= 1
    if (input.keysDown(39)) player.direction.x += 1
    if (input.keysDown(38)) player.direction.z -= 1
    if (input.keysDown(40)) player.direction.z += 1
    
  }
  
  def actionKey(): Unit = {
    player.doAction()
  }
  
  def clear(): Unit = {
    input.keyPressListener.subtractOne(" ", actionKey _)
  }
  
}