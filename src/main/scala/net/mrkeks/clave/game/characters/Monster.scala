package net.mrkeks.clave.game.characters

import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.view.ParticleSystem
import net.mrkeks.clave.game.abstracts._
import net.mrkeks.clave.game.objects.{Crate, CrateData}

import net.mrkeks.clave.util.markovIf
import net.mrkeks.clave.util.Mathf

import org.denigma.threejs.Sprite
import org.denigma.threejs.SpriteMaterial
import org.denigma.threejs.{Vector2, Vector3, Vector4}
import org.denigma.threejs.Texture
import org.denigma.threejs.Mesh
import org.denigma.threejs.Object3D
import org.denigma.threejs.MeshStandardMaterial
import org.denigma.threejs.Color

import org.scalajs.dom
import scala.scalajs.js.Any.fromFunction1
import net.mrkeks.clave.game.abstracts.FreezableObject

object Monster {
  var aggroMonsterMesh: Option[Object3D] = None
  DrawingContext.gltfLoader.load("gfx/monster01.glb", {gltf =>
    val mesh = gltf.scene.children(0).asInstanceOf[Object3D]
    aggroMonsterMesh = Some(mesh)
  })

  var defensiveMonsterMesh: Option[Object3D] = None
  DrawingContext.gltfLoader.load("gfx/monster02.glb", {gltf =>
    val mesh = gltf.scene.children(0).asInstanceOf[Object3D]
    defensiveMonsterMesh = Some(mesh)
  })

  def clear(): Unit = {

  }
}

class Monster(
    protected val map: GameMap,
    override val kind: MonsterData.MonsterKind = MonsterData.AggressiveMonster)
  extends GameObject with FreezableObject with PlaceableObject with MonsterData with ObjectShadow {

  import MonsterData._
  import PositionedObjectData._

  var context: DrawingContext = null
  val mesh: Object3D = new Object3D()
  var eyeMesh: Option[Object3D] = None

  var anim = 0.0
  var yScale = 0.0
  var rotate = 0.0
  
  var shadowSize = .85
  
  def init(context: DrawingContext): Unit = {
    this.context = context
    context.scene.add(mesh)
    initShadow(context)
    anim = 200.0 * Math.random()
    setState(Idle(2000 + 2000 * Math.random()))
    viewDirection = Direction.randomDirection()
  }
  
  def clear(context: DrawingContext): Unit = {
    removeFromMap()
    context.scene.remove(mesh)
    clearShadow(context)
  }
  
  def update(deltaTime: Double): Unit = {
    updateFreezable(deltaTime, context)

    // check whether something might push the monster away
    if (!state.isInstanceOf[PushedTo] && !state.isInstanceOf[Frozen]
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
          playerLikeObject <- map.getObjectsAt(pos).collect {
            case p: Player if p.isAlive() => p
            case c: Crate if c.kind == CrateData.PlayerLikeKind => c
          }
        } yield (pos, playerLikeObject)

        yScale = Mathf.approach(yScale, Math.sin(anim * .02) * .05, .003 * deltaTime)
        rotate = Mathf.approach(rotate, 0, .0001 * deltaTime)

        if (neighboringPlayers.nonEmpty && kind == AggressiveMonster) {
          // player approaches
          neighboringPlayers.headOption.foreach { case (pos, p) =>
            context.audio.play("monster-spots")
            setState(ChargeJumpTo(new Vector3(pos._1, 0, pos._2)))
          }
        } else if (map.getPlayerDangerousness(positionOnMap) > 3 && kind == FrightenedMonster) {
          // circled in
          val currentDanger = map.getPlayerDangerousness(positionOnMap)
          val options = for {
            pos <- map.getAdjacentPositions(positionOnMap)
            if !map.intersectsLevel(pos._1, pos._2, considerObstacles = true)
            newDanger = map.getPlayerDangerousness(pos)
            if newDanger < currentDanger
          } yield (newDanger, pos)
          if (options.nonEmpty) {
            val escapeRoute = options.minBy(_._1)._2
            val tar = new Vector3(escapeRoute._1, 0, escapeRoute._2)
            viewDirection = Direction.fromVec(tar.clone().sub(position))
            if (currentDanger > 10) context.audio.play("monster-evades")
            setState(MoveTo(tar))
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
          map.getObjectsAt(map.vecToMapPos(tar)).exists(o => o != this && o.isInstanceOf[Monster] || o.isInstanceOf[Player])) {
          // if tile became blocked while moving there, turn around.
          setState(MoveTo(position.clone().round()))
        } else {
          // breathing anim
          yScale = Mathf.approach(yScale, Math.sin(anim * .025) * .1, .003 * deltaTime)
          rotate = Mathf.approach(rotate, Math.sin(anim * .1) * .1, .001 * deltaTime)

          if (((anim - .1 * deltaTime) * 100).toInt % 3000 > (anim * 100).toInt % 3000) {
            context.audio.play("monster-moves")
          }

          val speed = (.002 - 0.0005 * sizeLevel) * deltaTime
          val newX = Mathf.approach(position.x, tar.x, speed)
          val newY = Mathf.approach(position.y, 0, speed)
          val newZ = Mathf.approach(position.z, tar.z, speed)
          setPosition(newX, newY , newZ)
          if (newX == tar.x && newZ == tar.z) {
            setState(Idle())
          }
        }
      case s @ ChargeJumpTo(tar, progress) =>
        if (progress >= .5 * sizeLevel) {
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
        if (ySpeed < -.0001 && map.intersectsLevel(tar, considerObstacles = true)) {
          // if tile became blocked while moving there either merge with the monster there or turn around
          val touchedOtherSmallMonsters = map.getObjectsAt((tar.x.toInt, tar.z.toInt)).collect {
            case m: Monster if m.sizeLevel < 2 && m.kind == this.kind => m
          }
          if (sizeLevel < 2 && touchedOtherSmallMonsters.nonEmpty) {
            context.audio.play("small-merges")
            setState(MergingWith(touchedOtherSmallMonsters.head))
          } else {
            setState(PushedTo(from, -ySpeed, forceful = true))
          }
        } // there is a bigger player there, turn around.
          else if (sizeLevel <= 1 && ySpeed < -.0004 && map.getObjectsAt((tar.x.toInt, tar.z.toInt)).exists(_.isInstanceOf[Player])) {
          context.audio.play("small-bumps")
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
      case s @ PushedTo(tar, ySpeed, forceful) =>
        val speed = .0025 * deltaTime
        val newX = Mathf.approach(position.x, tar.x, speed)
        val newZ = Mathf.approach(position.z, tar.z, speed)
        s.ySpeed -= .00004 * deltaTime
        yScale = Mathf.approach(yScale, Math.abs(s.ySpeed*10.0), .005 * deltaTime)
        setPosition(newX, position.y + ySpeed * deltaTime, newZ)
        rotate = Mathf.approach(rotate, 0, .0001 * deltaTime)
        if (newX == tar.x && newZ == tar.z) {
          if (forceful) {
            setState(Paralyzed(coolDown = 3000 + Math.random() * 2000))
          } else {
            setState(Idle(strollCoolDown = 1000 + Math.random() * 2000))
          }
          setPosition(newX, 0, newZ)
        }
      case s @ MergingWith(otherMonster, progress) =>
        s.progress += deltaTime * .01
        position.lerp(otherMonster.getPosition, progress)
        if (otherMonster.sizeLevel >= 2) {
          val tar = map.mapPosToVec(
              map.findNextFreeField(positionOnMap))
          setState(PushedTo(tar, tar.distanceTo(position) * 0.00004 / 0.0025 * .5))
        } else if (progress >= 1.0 ) {
          otherMonster.sizeLevel += 1
          context.particleSystem.burst("dust", (6 + 4 * Math.random()).toInt, ParticleSystem.BurstKind.Radial,
            new Vector3(position.x, position.y-.3, position.z), new Vector3(1, .1, 1),
            new Vector3(.0,.0,.0), new Vector3(-.002, .0, -.002), new Vector4(.1, .6, .1, .6), new Vector4(.2, .8, .2, .9), -.1, .0)
          markForDeletion()
        }
      case s @ Frozen(byCrate) =>
        removeFromMap()
        position.copy(byCrate.getPosition)
      case s @ Paralyzed(coolDown) =>
        s.coolDown -= deltaTime
        yScale = Mathf.approach(yScale, Math.sin(anim * .1) * .07, .009 * deltaTime)
        rotate = Mathf.approach(rotate, Math.sin(anim * .15) * coolDown / 2000.0, .003 * deltaTime)
        if (s.coolDown <= 0) {
          setState(Idle(strollCoolDown = 1000))
        }
    }

    // initialize mesh if necessary
    if (mesh.children.isEmpty) {
      kind match {
        case AggressiveMonster =>
          Monster.aggroMonsterMesh.foreach { m =>
            m.children.foreach(c => mesh.add(c.clone()))
            val mat = mesh.getObjectByName("Body").asInstanceOf[Mesh].material.asInstanceOf[MeshStandardMaterial].clone()
            val matMap = mat.map.clone()
            matMap.offset = new Vector2(Math.random(), 0)
            mat.color = new Color(.75 + .25 * Math.random(), .75 + .25 * Math.random(), .5 + .5 * Math.random())
            mat.map = matMap
            mesh.getObjectByName("Body").asInstanceOf[Mesh].material = mat
            matMap.needsUpdate = true
            mat.needsUpdate = true
            eyeMesh = Some(mesh.getObjectByName("Eyes"))
          }
        case FrightenedMonster =>
          Monster.defensiveMonsterMesh.foreach { m =>
            m.children.foreach(c => mesh.add(c.clone()))
            val mat = mesh.getObjectByName("Body").asInstanceOf[Mesh].material.asInstanceOf[MeshStandardMaterial].clone()
            val matMap = mat.map.clone()
            matMap.offset = new Vector2(Math.random(), 0)
            mat.color = new Color(.75 + .5 * Math.random(), .75 + .5 * Math.random(), .75 + .5 * Math.random())
            mat.map = matMap
            mesh.getObjectByName("Body").asInstanceOf[Mesh].material = mat
            matMap.needsUpdate = true
            mat.needsUpdate = true
            eyeMesh = Some(mesh.getObjectByName("Eyes"))
          }
      }
    }

    val meshPositionOffset = -.45 + sizeLevel * .25
    if (!state.isInstanceOf[Frozen]) {
      // particles for landing
      if (position.y < .5 && mesh.position.y - meshPositionOffset >= .5) {
        context.particleSystem.burst("dust", 6 + 2 * sizeLevel, ParticleSystem.BurstKind.Radial,
          new Vector3(position.x, position.y-.7, position.z), new Vector3(-.01, .1, -.01),
          new Vector3(.0,.0,.0), new Vector3(.003, .0, .003), new Vector4(.5, .5, .5, .6), new Vector4(.7, .7, .7, .5 + .1 * sizeLevel), .05 + .03 * sizeLevel, .1 + .05 * sizeLevel)
        if (sizeLevel <= 1) {
          context.audio.play("small-lands")
        } else {
          context.audio.play("big-lands")
        }
      } // particles for jumping
        else if (position.y > .01 && mesh.position.y - meshPositionOffset <= 0.01) {
        context.particleSystem.burst("dust", (3 + 2 * sizeLevel + 4 * Math.random()).toInt, ParticleSystem.BurstKind.Radial,
          new Vector3(position.x, position.y-.3, position.z), new Vector3(-.01, .1, -.01),
          new Vector3(.0,.0,.0), new Vector3(.002, .0, .002), new Vector4(.3, .5, .3, .6), new Vector4(.5, .7, .5, .3 + .1 * sizeLevel), -.2 + .05 * sizeLevel, + .05 * sizeLevel)
        if (sizeLevel <= 1) {
          context.audio.play("small-jumps")
        } else {
          context.audio.play("big-jumps")
        }
      }
    }
    mesh.position.set(position.x, position.y + meshPositionOffset, position.z)
    sizeLevel match {
      case 1 =>
        if (kind == FrightenedMonster && map.getPlayerDangerousness(positionOnMap) > 3) {
          val dangerScale = map.getPlayerDangerousness(positionOnMap) / 50.0
          mesh.scale.set(.9 - yScale * .2 + dangerScale * .5, 1.1 - dangerScale - yScale * .2, .9 + yScale + dangerScale * .9)
        } else {
          mesh.scale.set(.9 - yScale * .2, 1.0 - yScale * .2, .9 + yScale)
        }
      case _ =>
        mesh.scale.set(1.2 - yScale * .2, 1.3 - yScale * .2, 1.2 + yScale)
    }
    shadowSize = mesh.scale.y * .25 + .5
    mesh.rotation.y = Mathf.approach(mesh.rotation.y, Direction.toRadians(viewDirection), .01 * deltaTime, wraparound = 2.0 * Math.PI)
    mesh.rotation.z = rotate
  }

  override def freezeComplete(byCrate: CrateData): Boolean = {
    if (state.isInstanceOf[Frozen]) {
      false
    } else {
      setState(Frozen(byCrate))
      true
    }
  }

  def setState(newState: State): Unit = {
    state = newState match {
      case s @ MoveTo(_) =>
        s
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