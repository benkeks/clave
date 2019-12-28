package net.mrkeks.clave.game.characters

import net.mrkeks.clave.map.GameMap
import org.denigma.threejs.SpriteMaterial
import net.mrkeks.clave.view.DrawingContext
import org.denigma.threejs.Sprite
import net.mrkeks.clave.util.markovIf
import org.denigma.threejs.Vector3
import net.mrkeks.clave.util.Mathf
import org.denigma.threejs.Texture

import net.mrkeks.clave.game.GameObject
import net.mrkeks.clave.game.ObjectShadow
import net.mrkeks.clave.game.PositionedObject
import net.mrkeks.clave.game.PositionedObjectData
import scala.scalajs.js.Any.fromFunction1

object Monster {
  val material = new SpriteMaterial()
  
  var texture: Texture = null
  var textureBlink: Texture = null

  DrawingContext.textureLoader.load("gfx/monster.png", texture = _: Texture)
  DrawingContext.textureLoader.load("gfx/monster_blink.png", textureBlink = _: Texture)
  
  def clear() {
    material.dispose()
  }
}

class Monster(protected val map: GameMap)
  extends GameObject with PositionedObject with MonsterData with ObjectShadow {
  
  import MonsterData._
  import PositionedObjectData._
  
  val material = Monster.material.clone()
  
  val sprite = new Sprite(material)
  sprite.scale.set(1.4, 1.4, 1)
  
  var anim = 0.0
  var yScale = 0.0
  var rotate = 0.0
  
  val shadowSize = 1.0
  
  def init(context: DrawingContext) {
    context.scene.add(sprite)
    initShadow(context)
    anim = 200.0 * Math.random()
    setState(Idle(2000 + 2000 * Math.random()))
  }
  
  def clear(context: DrawingContext) {
    context.scene.remove(sprite)
    clearShadow(context)
  }
  
  def update(deltaTime: Double) {
    // check whether something might push the monster away
    if (!state.isInstanceOf[PushedTo] 
        && map.intersectsLevel(positionOnMap)) {
      val tar = map.mapPosToVec(
          map.findNextFreeField(positionOnMap))
      setState(PushedTo(tar, tar.distanceTo(position) * 0.00004 / 0.0025 * .5))
    }
    
    anim += .1 * deltaTime
    
    if (anim % 200 < 20.0) {
      material.map = Monster.textureBlink
    } else {
      material.map = Monster.texture
    }
    
    state match {
      case s @ Idle(strollCoolDown) =>
        val neighboringPlayers = for {
          pos <- map.getAdjacentPositions(positionOnMap)
          player <- map.getObjectsAt(pos).collect {case p: Player => p }
        } yield (pos, player)
        
        yScale = approach(yScale, Math.sin(anim * .02) * .05, .003 * deltaTime)
        rotate = approach(rotate, 0, .0001 * deltaTime)
        
        if (neighboringPlayers.nonEmpty) {
          // player approaches
          neighboringPlayers.headOption.foreach { case (pos, p) =>
             if (p.isAlive) {
               setState(ChargeJumpTo(new Vector3(pos._1, 0, pos._2)))
             }
          }
        } else markovIf (0.05 / (strollCoolDown + 1)) {
          // move into an arbitrary direction
          val tar = Direction.toVec(
              Direction.randomDirection()
            ).add(position)
          if (!map.intersectsLevel(tar, considerObstacles = true)) {
            setState(MoveTo(tar))
          }
        } markovElse {
          s.strollCoolDown = Math.max(s.strollCoolDown - deltaTime, 0.0)
        }
      case MoveTo(tar) =>
        if (map.intersectsLevel(tar, considerObstacles = false)) {
          // if tile became blocked while moving there, turn around.
          setState(MoveTo(position.clone().round()))
        } else {
          // breathing anim
          yScale = approach(yScale, Math.sin(anim * .025) * .1, .003 * deltaTime)
          rotate = approach(rotate, Math.sin(anim * .1) * .1, .001 * deltaTime)
          
          val speed = .001 * deltaTime
          val newX = approach(position.x, tar.x, speed)
          val newY = approach(position.y, 0, speed)
          val newZ = approach(position.z, tar.z, speed)
          setPosition(newX, newY , newZ)
          if (newX == tar.x && newZ == tar.z) {
            setState(Idle())
          }
        }
      case s @ ChargeJumpTo(tar, progress) =>
        if (progress >= 1.0) {
          position.setY(0.0)
          setState(JumpTo(tar, 
              from = position.clone(),
              ySpeed = tar.distanceTo(position) * 0.00004 / 0.0025 * .5))
        } else {
          s.progress += .001 * deltaTime
          val newY = .3 * Mathf.pingpong(progress * 3.0)
          yScale = approach(yScale, Math.abs(newY-.2)*1.0, .003 * deltaTime)
          rotate = approach(rotate, 0, .0001 * deltaTime)
          position.setY(newY)
        }
      case s @ JumpTo(tar, from, ySpeed) =>
        if (map.intersectsLevel(tar, considerObstacles = true)) {
          // if tile became blocked while moving there, turn around.
          setState(PushedTo(from, -ySpeed))
        } else {
          val speed = .0025 * deltaTime
          val newX = approach(position.x, tar.x, speed)
          val newZ = approach(position.z, tar.z, speed)
          s.ySpeed -= .00004 * deltaTime
          yScale = approach(yScale, Math.abs(s.ySpeed*10.0), .005 * deltaTime)
          rotate = approach(rotate, 0, .0001 * deltaTime)
          // in flight don't update the map placement!
          position.set(newX, position.y + ySpeed * deltaTime, newZ)
          if (newX == tar.x && newZ == tar.z) {
            // give some extra cooldown after jumping
            setState(Idle(strollCoolDown = 4000))
            setPosition(newX, 0, newZ)
          }
        }
      case s @ PushedTo(tar, ySpeed) =>
        val speed = .0025 * deltaTime
        val newX = approach(position.x, tar.x, speed)
        val newZ = approach(position.z, tar.z, speed)
        s.ySpeed -= .00004 * deltaTime
        yScale = approach(yScale, Math.abs(s.ySpeed*10.0), .005 * deltaTime)
        setPosition(newX, position.y + ySpeed * deltaTime, newZ)
        rotate = approach(rotate, 0, .0001 * deltaTime)
        if (newX == tar.x && newZ == tar.z) {
          // give fewer cooldown after being pushed
          setState(Idle(strollCoolDown = 2000))
          setPosition(newX, 0, newZ)
        }
    }
    sprite.position.set(position.x, position.y + .2, position.z + .3)
    sprite.scale.set(1.4 - yScale * .5, 1.4 + yScale, 1)
    sprite.material.rotation = rotate
    updateShadow()
  }
  
  def approach(src: Double, tar: Double, speed: Double) =
    if (Math.abs(tar - src) <= speed) tar else src + speed * Math.signum(tar - src)
  
  def setState(newState: State) {
    state = newState match {
      case s => s
    }
  }
}