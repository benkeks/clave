package net.mrkeks.clave.game

import org.denigma.threejs.Vector2
import net.mrkeks.clave.view.Input

class PlayerControl(val player: Player, val input: Input) {
  
  input.keyPressListener.addBinding(32, actionKey)
  
  def update(deltaTime: Double) {
    player.direction.set(0,0)
    
    if (input.keysDown(37)) player.direction.x -= 1
    if (input.keysDown(39)) player.direction.x += 1
    if (input.keysDown(38)) player.direction.y -= 1
    if (input.keysDown(40)) player.direction.y += 1
    
  }
  
  def actionKey() {
    player.doAction()
  }
  
  def clear() {
    input.keyPressListener.removeBinding(32, actionKey)
  }
  
}