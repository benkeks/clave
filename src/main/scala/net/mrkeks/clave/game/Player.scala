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
  
  import PlayerData._
  import PositionedObjectData._
  
  var nextField = (0,0)
  
  val sprite = new Sprite(Player.material)
  
  def init(context: DrawingContext) {
    context.scene.add(sprite)
  }
  
  def clear(context: DrawingContext) {
    context.scene.remove(sprite)
  }
  
  def update(deltaTime: Double) {
    sprite.position.copy(position)
    
    move(direction.multiplyScalar(state.speed * deltaTime)) // WARNING: destroys direction!
  }
  
  def move(dir: Vector2) {
    if (dir.x != 0 || dir.y != 0) {
      viewDirection = Direction.fromVec(dir)
      val newPos2d = map.localSlideCast(new Vector2(position.x, position.z), dir)
      setPosition(newPos2d.x, position.y, newPos2d.y)
      nextField = map.vecToMapPos(Direction.toVec(viewDirection) add newPos2d)
      updateTouch()
    }
  }
  
  def updateTouch() {
    val neighboringObjects = map.getObjectsAt(nextField)
    
    touching match {
      case Some(o) if !neighboringObjects.contains(o) =>
        // lost touch!
        mutualUntouch()
      case _ =>
    }
    
    if (touching.isEmpty) {
      val newTouchObj = neighboringObjects.collectFirst({case c: Crate => c})
      
      newTouchObj.foreach(touch)
    }
  }
  
  def doAction() {
    state match {
      case Idle() =>
        touching match {
          case Some(c: Crate) =>
            pickup(c)
          case _ =>
            // nothing to grab
        }
      case Carrying(c: Crate) =>
        place(c)
      case _ =>
    }
  }
  
  def pickup(crate: Crate) {
    state = Carrying(crate)
    crate.pickup(this)
  }
  
  def place(crate: Crate) {
    if ((crate.place _).tupled(nextField)) {
      state = Idle()
    }
  }
}