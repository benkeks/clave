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

class Player(protected val map: GameMap)
  extends GameObject with PlayerData with PositionedObject {  
  
  import PlayerData.Direction
  
  var nextField = (0,0)
  
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
    if (dir.x != 0 || dir.y != 0) {
      viewDirection = Direction.fromVec(dir)
      val newPos2d = map.localSlideCast(new Vector2(position.x, position.z), dir)
      position.set(newPos2d.x, position.y, newPos2d.y)
      updatePositionOnMap()
      nextField = map.vecToMapPos(Direction.toVec(viewDirection) add newPos2d)
    }
  }
}