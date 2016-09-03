package net.mrkeks.clave.game

class PlayerControl(val player: Player, val input: Input) {
  
  def update(deltaTime: Double) {
    if (input.keysDown(37)) player.position.x -= .02*deltaTime
    if (input.keysDown(39)) player.position.x += .02*deltaTime
    if (input.keysDown(38)) player.position.z -= .02*deltaTime
    if (input.keysDown(40)) player.position.z += .02*deltaTime
  }
  
}