package net.mrkeks.clave.game

import net.mrkeks.clave.view.DrawingContext
import org.denigma.threejs.SpriteMaterial
import org.denigma.threejs.Sprite
import org.denigma.threejs.Vector2
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.game.objects.Crate
import org.denigma.threejs.MeshLambertMaterial
import org.denigma.threejs.BoxGeometry
import org.denigma.threejs.Mesh
import org.denigma.threejs.TextureLoader
import org.denigma.threejs.Texture

object Player {
  val material = new SpriteMaterial()
  //material.color.setHex(0xee0000)
  
  new TextureLoader().load("gfx/player_monster.png", { tex: Texture =>
    material.map = tex
  })
  
  private val dropPreviewMaterial = new MeshLambertMaterial()
  dropPreviewMaterial.transparent = true
  dropPreviewMaterial.opacity = .5
  dropPreviewMaterial.color.setHex(0xaa2299)
  
  private val dropPreviewGeometry = new BoxGeometry(1.1, .1, 1.1)
  
  def clear() {
    material.dispose()
    dropPreviewMaterial.dispose()
  }
}

class Player(protected val map: GameMap)
  extends GameObject with PlayerData with PositionedObject with ObjectShadow {  
  
  import PlayerData._
  import PositionedObjectData._
  
  var nextField = (0,0)
  
  var anim = 0.0
  
  val sprite = new Sprite(Player.material)
  
  val dropPreview = new Mesh(Player.dropPreviewGeometry, Player.dropPreviewMaterial)
  
  def init(context: DrawingContext) {
    context.scene.add(sprite)
    context.scene.add(dropPreview)
    initShadow(context)
  }
  
  def clear(context: DrawingContext) {
    context.scene.remove(sprite)
    context.scene.remove(dropPreview)
    clearShadow(context)
  }
  
  def update(deltaTime: Double) {
    // drop preview visibility defaults to false
    dropPreview.visible = false
    
    state match {
      case Idle()  =>
        anim += (state.speed * direction.length() + .001) * deltaTime
        move(direction.multiplyScalar(state.speed * deltaTime)) // WARNING: destroys direction!
        if (map.isMonsterOn(positionOnMap)) {
          setState(Dead())
        }
      case Carrying(crate: Crate) =>
        anim += state.speed * deltaTime
        if (crate.canBePlaced(nextField._1, nextField._2)) {
          dropPreview.visible = true
          dropPreview.position.copy(map.mapPosToVec(nextField))
          dropPreview.position.setY(-.5)
        }
        move(direction.multiplyScalar(state.speed * deltaTime)) // WARNING: destroys direction!
        if (map.isMonsterOn(positionOnMap)) {
          setState(Dead())
        }
      case Dead() =>
        
      case _ =>
    }
    sprite.position.copy(position)
    sprite.scale.setY(1.0 + Math.sin(anim * 2) * .1)
    sprite.scale.setZ(1.0 - Math.sin(anim * 2 + .2) * .05)
    updateShadow()
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
      case Dead() =>
        
      case _ =>
    }
  }
  
  def pickup(crate: Crate) {
    setState(Carrying(crate))
    crate.pickup(this)
  }
  
  def place(crate: Crate) {
    if ((crate.place _).tupled(nextField)) {
      setState(Idle())
    }
  }
  
  def setState(newState: State) {
    state = newState
  }
}