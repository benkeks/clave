package net.mrkeks.clave.game

import net.mrkeks.clave.view.DrawingContext
import org.denigma.threejs.SpriteMaterial
import org.denigma.threejs.Sprite

object Player {
  val material = new SpriteMaterial()
  material.color.setHex(0xee0000)
  
  def clear() {
    material.dispose()
  }
}

class Player extends GameObject {  
  
  val sprite = new Sprite(Player.material)
  
  def init(context: DrawingContext) {
    context.scene.add(sprite)
  }
  
  def clear() {
  }
  
  def update(deltaTime: Double) {
    sprite.position.set(position.x, position.y, position.z)
  }
}