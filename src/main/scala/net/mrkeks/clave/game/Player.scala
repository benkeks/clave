package net.mrkeks.clave.game

import net.mrkeks.clave.view.DrawingContext
import org.denigma.threejs.SpriteMaterial
import org.denigma.threejs.Sprite
import org.denigma.threejs.Vector2
import net.mrkeks.clave.map.GameMap

object Player {
  val material = new SpriteMaterial()
  material.color.setHex(0xee0000)
  
  def clear() {
    material.dispose()
  }
}

class Player(map: GameMap)
  extends GameObject with PlayerData {  
  
  val sprite = new Sprite(Player.material)
  
  def init(context: DrawingContext) {
    context.scene.add(sprite)
  }
  
  def clear() {
  }
  
  def update(deltaTime: Double) {
    sprite.position.set(position.x, position.y, position.z)
    
    move(direction.multiplyScalar(.01 * deltaTime)) // WARNING: destroys direction!
  }
  
  def move(dir: Vector2) {
    val newPos2d = map.localSlideCast(new Vector2(position.x, position.z), dir)
    position.set(newPos2d.x, position.y, newPos2d.y)
  }
}