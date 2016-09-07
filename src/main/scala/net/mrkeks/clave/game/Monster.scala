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
  
  import MonsterData._
  import PositionedObjectData._
  
  val sprite = new Sprite(Monster.material)
  
  def init(context: DrawingContext) {
    context.scene.add(sprite)
  }
  
  def clear(context: DrawingContext) {
    context.scene.remove(sprite)
  }
  
  def update(deltaTime: Double) {
    state match {
      case Idle() =>
        val rnd = Math.random()
        if (rnd < .05) {
          val tar = Direction.toVec3(
              Direction.fromRnd(rnd / .05)
            ).add(position)
          if (!map.intersectsLevel(tar)) {
            setState(MoveTo(tar))
          }
        }
      case MoveTo(tar) =>
        if (map.intersectsLevel(tar)) {
          // if tile became blocked while moving there, turn around.
          setState(MoveTo(position.clone().round()))
        } else {
          val speed = .001 * deltaTime
          val newX = approach(position.x, tar.x, speed)
          val newZ = approach(position.z, tar.z, speed)
          setPosition(newX, position.y, newZ)
          if (newX == tar.x && newZ == tar.z) {
            setState(Idle())
          }
        }
    }
    
    sprite.position.copy(position)
    
    updatePositionOnMap()
  }
  
  def approach(src: Double, tar: Double, speed: Double) =
    if (Math.abs(tar - src) <= speed) tar else src + speed * Math.signum(tar - src)
  
  def setState(newState: State) {
    state = newState
  }
}