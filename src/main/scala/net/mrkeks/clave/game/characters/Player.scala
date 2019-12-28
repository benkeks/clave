package net.mrkeks.clave.game.characters

import net.mrkeks.clave.view.DrawingContext
import org.denigma.threejs.SpriteMaterial
import org.denigma.threejs.Sprite
import org.denigma.threejs.Vector3
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.game.objects.Crate
import org.denigma.threejs.MeshLambertMaterial
import org.denigma.threejs.BoxGeometry
import org.denigma.threejs.Mesh
import org.denigma.threejs.Texture
import net.mrkeks.clave.game.GameObject
import net.mrkeks.clave.game.ObjectShadow
import net.mrkeks.clave.game.PositionedObject
import net.mrkeks.clave.game.PositionedObjectData
import scala.scalajs.js.Any.fromFunction1

object Player {
  val material = new SpriteMaterial()
  
  DrawingContext.textureLoader.load("gfx/player_monster.png", { tex: Texture =>
    material.map = tex
    material.needsUpdate = true
  })
  
  private val dropPreviewMaterial = new MeshLambertMaterial()
  dropPreviewMaterial.transparent = true
  dropPreviewMaterial.opacity = .5
  dropPreviewMaterial.color.setHex(0xaa2299)
  
  private val dropPreviewGeometry = new BoxGeometry(1.1, .1, 1.1)
  
  def clear(): Unit = {
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
  
  val shadowSize = 0.7
  
  def init(context: DrawingContext): Unit = {
    context.scene.add(sprite)
    context.scene.add(dropPreview)
    initShadow(context)
  }
  
  def clear(context: DrawingContext): Unit = {
    context.scene.remove(sprite)
    context.scene.remove(dropPreview)
    clearShadow(context)
  }
  
  def update(deltaTime: Double): Unit = {
    dropPreview.visible = false
    
    sprite.position.set(position.x, position.y, position.z + .3)

    if (map.intersectsLevel(positionOnMap)) {
      val tar = map.mapPosToVec(
          map.findNextFreeField(positionOnMap))
      move(tar.sub(position).setLength(.01 * deltaTime))
    }

    state match {
      case Spawning(ySpeed) =>
        setPosition(position.x, position.y + ySpeed * deltaTime, position.z)
        if (position.y <= 0) {
          setPosition(position.x, 0, position.z)
          setState(Idle())
        }
      case Idle()  =>
        anim += (state.speed * direction.length() + .001) * deltaTime
        move(direction.clone().multiplyScalar(state.speed * deltaTime))
        if (map.isMonsterOn(positionOnMap)) {
          setState(Dead())
        }
        sprite.scale.setY(1.0 + Math.sin(anim * 2) * .1)
        sprite.scale.setZ(1.0 - Math.sin(anim * 2 + .2) * .05)
      case Carrying(crate: Crate) =>
        anim += state.speed * deltaTime
        if (crate.canBePlaced(nextField._1, nextField._2)) {
          dropPreview.visible = true
          dropPreview.position.copy(map.mapPosToVec(nextField))
          dropPreview.position.setY(-.5)
        }
        move(direction.clone().multiplyScalar(state.speed * deltaTime))
        if (map.isMonsterOn(positionOnMap)) {
          setState(Dead())
        }
        sprite.scale.setY(1.0 + Math.sin(anim * 2) * .1)
        sprite.scale.setZ(1.0 - Math.sin(anim * 2 + .2) * .05)
      case s @ Dead() =>
        s.deathAnim = Math.max(s.deathAnim - .005 * deltaTime, .66)
        sprite.position.set(position.x, position.y - .8 + s.deathAnim * .8, position.z + .3)
        sprite.scale.setY((1.0 + Math.sin(anim * 2) * .1) * s.deathAnim)
        sprite.scale.setX((1.0 - Math.sin(anim * 2 + .2) * .05) / s.deathAnim)
      case _ =>
    }
    
    updateShadow()
  }
  
  /** Transforms the player in the plane. (The y component of the Vec3 will be ignored!) */
  def move(dir: Vector3): Unit = {
    if (dir.x != 0 || dir.z != 0) {
      viewDirection = Direction.fromVec(dir)
      val newPos = map.localSlideCast(position, dir,
        bumpingDist = if (state.isInstanceOf[Carrying]) .8 else .4 )
      setPosition(newPos.x, position.y, newPos.z)
      nextField = map.vecToMapPos(Direction.toVec(viewDirection) add newPos)
      updateTouch()
    }
  }
  
  def updateTouch(): Unit = {
    val neighboringObjects = map.getObjectsAt(nextField)
    
    touching match {
      case Some(o) if !neighboringObjects.contains(o) =>
        // lost touch!
        mutualUntouch()
      case _ =>
    }
    
    if (touching.isEmpty) {
      val newTouchObj = neighboringObjects.collectFirst { case c: Crate => c }
      
      newTouchObj.foreach(touch)
    }
  }
  
  def doAction(): Unit = {
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
  
  def pickup(crate: Crate): Unit = {
    setState(Carrying(crate))
    crate.pickup(this)
  }
  
  def place(crate: Crate): Unit = {
    if ((crate.place _).tupled(nextField)) {
      setState(Idle())
    }
  }
  
  def setState(newState: State): Unit = {
    state = newState match {
      case Dead() =>
        state match {
          case Carrying(c) =>
            place(c.asInstanceOf[Crate])
          case _ =>
        }
        newState
      case Spawning(ySpeed) =>
        if (ySpeed < 0) {
          setPosition(position.x, 50, position.z)
        }
        newState
      case s => s
    }
  }
}