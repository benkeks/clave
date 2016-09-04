package net.mrkeks.clave.game

import net.mrkeks.clave.map.GameMap
import org.denigma.threejs.SpriteMaterial
import net.mrkeks.clave.view.DrawingContext
import org.denigma.threejs.Sprite

object Monster {
  val material = new SpriteMaterial()
  material.color.setHex(0x117711)
  
  def clear() {
    material.dispose()
  }
}

class Monster(protected val map: GameMap)
  extends GameObject with PositionedObject with MonsterData {
  
  val sprite = new Sprite(Monster.material)
  
  def init(context: DrawingContext) {
    context.scene.add(sprite)
  }
  
  def clear() {
  }
  
  def update(deltaTime: Double) {
    sprite.position.copy(position)
    updatePositionOnMap()
  }
  
}