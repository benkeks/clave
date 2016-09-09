package net.mrkeks.clave.game

import net.mrkeks.clave.map.GameMap
import org.denigma.threejs.SpriteMaterial
import net.mrkeks.clave.view.DrawingContext
import org.denigma.threejs.Sprite
import net.mrkeks.clave.util.markovIf
import org.denigma.threejs.Vector3
import net.mrkeks.clave.util.Mathf

object Monster {
  val material = new SpriteMaterial()
  material.color.setHex(0x117711)
  
  def clear() {
    material.dispose()
  }
}

class Monster(protected val map: GameMap)
  extends GameObject with PositionedObject with MonsterData with ObjectShadow {
  
  import MonsterData._
  import PositionedObjectData._
  
  val sprite = new Sprite(Monster.material)
  
  def init(context: DrawingContext) {
    context.scene.add(sprite)
    initShadow(context)
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
    
    state match {
      case s @ Idle(strollCoolDown) =>
        val neighboringPlayers = for {
          pos <- map.getAdjacentPositions(positionOnMap)
          player <- map.getObjectsAt(pos).collect {case p: Player => p }
        } yield (pos, player)
        
        if (neighboringPlayers.nonEmpty) {
          // player approaches
          neighboringPlayers.headOption.foreach { case (pos, p) =>
            setState(ChargeJumpTo(new Vector3(pos._1, 0, pos._2)))
          }
        } else markovIf (0.05 / (strollCoolDown + 1)) {
          // move into an arbitrary direction
          val tar = Direction.toVec3(
              Direction.randomDirection()
            ).add(position)
          if (!map.intersectsLevel(tar)) {
            setState(MoveTo(tar))
          }
        } markovElse {
          s.strollCoolDown = Math.max(s.strollCoolDown - deltaTime, 0.0)
        }
      case MoveTo(tar) =>
        if (map.intersectsLevel(tar)) {
          // if tile became blocked while moving there, turn around.
          setState(MoveTo(position.clone().round()))
        } else {
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
          position.setY(.3 * Mathf.pingpong(progress * 3.0))
        }
      case s @ JumpTo(tar, from, ySpeed) =>
        if (map.intersectsLevel(tar)) {
          // if tile became blocked while moving there, turn around.
          setState(PushedTo(from, -ySpeed))
        } else {
          val speed = .0025 * deltaTime
          val newX = approach(position.x, tar.x, speed)
          val newZ = approach(position.z, tar.z, speed)
          s.ySpeed -= .00004 * deltaTime
          // in flight don't update the map placement!
          position.set(newX, position.y + ySpeed * deltaTime, newZ)
          if (newX == tar.x && newZ == tar.z) {
            // give some extra cooldown after jumping
            setState(Idle(strollCoolDown = 3000))
            setPosition(newX, 0, newZ)
          }
        }
      case s @ PushedTo(tar, ySpeed) =>
        val speed = .0025 * deltaTime
        val newX = approach(position.x, tar.x, speed)
        val newZ = approach(position.z, tar.z, speed)
        s.ySpeed -= .00004 * deltaTime
        setPosition(newX, position.y + ySpeed * deltaTime, newZ)
        if (newX == tar.x && newZ == tar.z) {
          // give fewer cooldown after being pushed
          setState(Idle(strollCoolDown = 1000))
          setPosition(newX, 0, newZ)
        }
    }
    sprite.position.copy(position)
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