package net.mrkeks.clave.game.characters

import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.game.GameObject
import net.mrkeks.clave.game.ObjectShadow
import net.mrkeks.clave.game.PositionedObject
import net.mrkeks.clave.game.PositionedObjectData
import net.mrkeks.clave.game.PlaceableObject

import net.mrkeks.clave.util.markovIf
import net.mrkeks.clave.util.Mathf

import org.denigma.threejs.Sprite
import org.denigma.threejs.SpriteMaterial
import org.denigma.threejs.{Vector2, Vector3}
import org.denigma.threejs.Texture
import org.denigma.threejs.Mesh
import org.denigma.threejs.Object3D
import org.denigma.threejs.MeshStandardMaterial
import org.denigma.threejs.Color

import org.scalajs.dom
import scala.scalajs.js.Any.fromFunction1

object Monster {
  var monsterMesh: Option[Object3D] = None
  DrawingContext.gltfLoader.load("gfx/monster01.glb", {gltf =>
    val mesh = gltf.scene.children(0).asInstanceOf[Object3D]
    monsterMesh = Some(mesh)
  })

  def clear(): Unit = {
  }
}

class Monster(protected val map: GameMap)
  extends GameObject with PlaceableObject with MonsterData with ObjectShadow {

  import MonsterData._
  import PositionedObjectData._

  val mesh: Object3D = new Object3D()
  var eyeMesh: Option[Object3D] = None

  var anim = 0.0
  var yScale = 0.0
  var rotate = 0.0
  
  val shadowSize = .85
  
  def init(context: DrawingContext): Unit = {
    context.scene.add(mesh)
    initShadow(context)
    anim = 200.0 * Math.random()
    setState(Idle(2000 + 2000 * Math.random()))
    viewDirection = Direction.randomDirection()
  }
  
  def clear(context: DrawingContext): Unit = {
    context.scene.remove(mesh)
    clearShadow(context)
  }
  
  def update(deltaTime: Double): Unit = {
    // check whether something might push the monster away
    if (!state.isInstanceOf[PushedTo]
        && map.intersectsLevel(positionOnMap)) {
      val tar = map.mapPosToVec(
          map.findNextFreeField(positionOnMap))
      setState(PushedTo(tar, tar.distanceTo(position) * 0.00004 / 0.0025 * .5))
    }
    
    anim += .1 * deltaTime
    
    if (anim % 200 < 20.0) {
      eyeMesh.foreach( e => e.scale.y = .2)
    } else {
      eyeMesh.foreach( e => e.scale.y = 1.0)
    }
    
    state match {
      case s @ Idle(strollCoolDown) =>
        val neighboringPlayers = for {
          pos <- map.getAdjacentPositions(positionOnMap)
          player <- map.getObjectsAt(pos).collect {case p: Player => p }
        } yield (pos, player)
        
        yScale = Mathf.approach(yScale, Math.sin(anim * .02) * .05, .003 * deltaTime)
        rotate = Mathf.approach(rotate, 0, .0001 * deltaTime)
        
        if (neighboringPlayers.nonEmpty) {
          // player approaches
          neighboringPlayers.headOption.foreach { case (pos, p) =>
             if (p.isAlive) {
               setState(ChargeJumpTo(new Vector3(pos._1, 0, pos._2)))
             }
          }
        } else markovIf (0.0035) {
          // move into an arbitrary direction
          viewDirection = Direction.randomDirection()
        }.markovElseIf (0.05 / (strollCoolDown + 1)) {
          // move into an arbitrary direction
          val tar = Direction.toVec(viewDirection).add(position)
          if (!map.intersectsLevel(tar, considerObstacles = true)) {
            setState(MoveTo(tar))
          }
        } markovElse {
          s.strollCoolDown = Math.max(s.strollCoolDown - deltaTime, 0.0)
        }
      case MoveTo(tar) =>
        if (map.intersectsLevel(tar, considerObstacles = false) ||
          map.getObjectsAt(map.vecToMapPos(tar)).exists(o => o != this && o.isInstanceOf[Monster])) {
          // if tile became blocked while moving there, turn around.
          setState(MoveTo(position.clone().round()))
        } else {
          // breathing anim
          yScale = Mathf.approach(yScale, Math.sin(anim * .025) * .1, .003 * deltaTime)
          rotate = Mathf.approach(rotate, Math.sin(anim * .1) * .1, .001 * deltaTime)

          val speed = .001 * deltaTime
          val newX = Mathf.approach(position.x, tar.x, speed)
          val newY = Mathf.approach(position.y, 0, speed)
          val newZ = Mathf.approach(position.z, tar.z, speed)
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
          yScale = Mathf.approach(yScale, Math.abs(newY-.2)*1.0, .003 * deltaTime)
          rotate = Mathf.approach(rotate, 0, .0001 * deltaTime)
          position.setY(newY)
        }
      case s @ JumpTo(tar, from, ySpeed) =>
        if (map.intersectsLevel(tar, considerObstacles = true)) {
          // if tile became blocked while moving there, turn around.
          setState(PushedTo(from, -ySpeed))
        } else {
          val speed = .0025 * deltaTime
          val newX = Mathf.approach(position.x, tar.x, speed)
          val newZ = Mathf.approach(position.z, tar.z, speed)
          s.ySpeed -= .00004 * deltaTime
          yScale = Mathf.approach(yScale, Math.abs(s.ySpeed*10.0), .005 * deltaTime)
          rotate = Mathf.approach(rotate, 0, .0001 * deltaTime)
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
        val newX = Mathf.approach(position.x, tar.x, speed)
        val newZ = Mathf.approach(position.z, tar.z, speed)
        s.ySpeed -= .00004 * deltaTime
        yScale = Mathf.approach(yScale, Math.abs(s.ySpeed*10.0), .005 * deltaTime)
        setPosition(newX, position.y + ySpeed * deltaTime, newZ)
        rotate = Mathf.approach(rotate, 0, .0001 * deltaTime)
        if (newX == tar.x && newZ == tar.z) {
          // give fewer cooldown after being pushed
          setState(Idle(strollCoolDown = 2000))
          setPosition(newX, 0, newZ)
        }
    }

    if (mesh.children.isEmpty) {
      Monster.monsterMesh.foreach { m =>
        m.children.foreach(c => mesh.add(c.clone()))
        val mat = mesh.getObjectByName("Body").asInstanceOf[Mesh].material.asInstanceOf[MeshStandardMaterial].clone()
        val matMap = mat.map.clone()
        matMap.offset = new Vector2(Math.random(), 0)
        mat.color = new Color(.75 + .25 * Math.random(), .75 + .25 * Math.random(), .5 + .5 * Math.random())
        mat.map = matMap
        mesh.getChildByName("Body").asInstanceOf[Mesh].material = mat
        matMap.needsUpdate = true
        mat.needsUpdate = true
        eyeMesh = Some(mesh.getObjectByName("Eyes"))
      }
    }
    mesh.position.set(position.x, position.y + .2, position.z)
    mesh.scale.set(1.2 - yScale * .2, 1.2 - yScale * .2, 1.2 + yScale)
    mesh.rotation.y = Mathf.approach(mesh.rotation.y, Direction.toRadians(viewDirection), .01 * deltaTime, wraparound = 2.0 * Math.PI)
    mesh.rotation.z = rotate
    updateShadow()
  }
  
  

  def setState(newState: State): Unit = {
    state = newState match {
      case s @ JumpTo(tar, from, ySpeed) =>
        viewDirection = Direction.fromVec(tar.clone().sub(position))
        s
      case s @ ChargeJumpTo(tar, progress) =>
        viewDirection = Direction.fromVec(tar.clone().sub(position))
        s
      case s => s
    }
  }
}