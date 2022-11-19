package net.mrkeks.clave.game.characters

import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.view.ParticleSystem
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.game.objects.Crate
import net.mrkeks.clave.game.abstracts._
import net.mrkeks.clave.util.Mathf

import org.denigma.threejs.SpriteMaterial
import org.denigma.threejs.Sprite
import org.denigma.threejs.{Vector3, Vector4}
import org.denigma.threejs.MeshLambertMaterial
import org.denigma.threejs.BoxGeometry
import org.denigma.threejs.Mesh
import org.denigma.threejs.Object3D
import org.denigma.threejs.Texture

import scala.scalajs.js.Any.fromFunction1
import net.mrkeks.clave.game.objects.CrateData

object Player {
  val material = new SpriteMaterial()
  
  DrawingContext.textureLoader.load("gfx/player_monster.png", { tex: Texture =>
    material.map = tex
    material.needsUpdate = true
  })

  var playerMesh: Option[Object3D] = None
  DrawingContext.gltfLoader.load("gfx/player_monster.glb", {gltf =>
    val mesh = gltf.scene.children(0).asInstanceOf[Object3D]
    playerMesh = Some(mesh)
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
  extends GameObject with PlayerData with FreezableObject with PositionedObject with ObjectShadow {
  
  import PlayerData._
  import PositionedObjectData._
  
  var nextField = (0,0)
  
  var anim = 0.0
  var movementDelta = new Vector3()

  var context: DrawingContext = null
  val sprite = new Sprite(Player.material)
  val mesh: Object3D = new Object3D()
  var eyeMesh: Option[Object3D] = None
  
  val dropPreview = new Mesh(Player.dropPreviewGeometry, Player.dropPreviewMaterial)
  
  val shadowSize = 0.7
  
  def init(context: DrawingContext): Unit = {
    this.context = context
    context.scene.add(mesh)
    context.scene.add(dropPreview)
    initShadow(context)
  }
  
  def clear(context: DrawingContext): Unit = {
    context.scene.remove(mesh)
    context.scene.remove(dropPreview)
    clearShadow(context)
  }
  
  def update(deltaTime: Double): Unit = {
    dropPreview.visible = false

    updateFreezable(deltaTime, context)

    if (mesh.children.isEmpty) {
      Player.playerMesh.foreach { m =>
        m.children.foreach(c => mesh.add(c.clone()))
        eyeMesh = Some(mesh.getObjectByName("Eyes"))
      }
    }

    val newMovementDelta = mesh.position.clone()

    // place on map and look in direction
    mesh.position.set(position.x, position.y - .2, position.z)
    mesh.rotation.y = Mathf.approach(mesh.rotation.y, Direction.toRadians(viewDirection), .02 * deltaTime, wraparound = 2.0 * Math.PI)

    if (map.intersectsLevel(positionOnMap)) {
      val tar = map.mapPosToVec(
          map.findNextFreeField(positionOnMap))
      move(tar.sub(position).setLength(.01 * deltaTime))
    }

    newMovementDelta.sub(mesh.position).multiplyScalar(-1.0)

    val targetSpeed = state.speed * direction.length()

    // walking animation
    if (targetSpeed > 0.001) {
      mesh.rotation.z = Mathf.approach(mesh.rotation.z, Math.sin(anim * 2) * .2, .03 * deltaTime)
    } else {
      mesh.rotation.z = Mathf.approach(mesh.rotation.z, 0, .002 * deltaTime)
    }

    // create dust if changing direction
    if (state.speed > 0.001 && (
        movementDelta.x > .0001 && newMovementDelta.x <= 0 ||
        movementDelta.x < -.0001 && newMovementDelta.x >= 0 ||
        movementDelta.z > .0001 && newMovementDelta.z <= 0 ||
        movementDelta.z < -.0001 && newMovementDelta.z >= 0)) {
      movementDelta.multiplyScalar(.05)
      val dustPos = position.clone()
      dustPos.y = -.4
      context.particleSystem.burst("dust", 7, ParticleSystem.BurstKind.Box,
        dustPos, dustPos,
        new Vector3(movementDelta.x-.002,.0,movementDelta.z-.002), new Vector3(movementDelta.x+.002, .0, movementDelta.z+.002), new Vector4(.6, .6, .6, .4), new Vector4(.8, .8, .8, .5), .1, .2)
    }
    movementDelta.copy(newMovementDelta)

    state match {
      case Spawning(ySpeed) =>
        setPosition(position.x, position.y + ySpeed * deltaTime, position.z)
        if (position.y <= 0) {
          setPosition(position.x, 0, position.z)
          context.audio.play("small-lands")
          setState(Idle())
        }
      case Idle() =>
        val newAnim = anim + (targetSpeed + .001) * deltaTime
        if (targetSpeed > .0001 && (anim * 100).toInt % 120 > (newAnim * 100).toInt % 120) {
          context.audio.play("player-moves")
        }
        anim = newAnim
        move(direction.clone().multiplyScalar(state.speed * deltaTime))
        if (isHarmedByMonster(positionOnMap)) {
          setState(Dead())
        }
        mesh.scale.setY(1.0 + Math.sin(anim * 2) * .2)
        mesh.scale.setZ(1.0 - Math.sin(anim * 2 + .3) * .05)
      case Carrying(crate: Crate) =>
        val newAnim = anim + (targetSpeed + .001) * deltaTime
        if (targetSpeed > .0001 && (anim * 100).toInt % 120 > (newAnim * 100).toInt % 120) {
          context.audio.play("player-moves")
        }
        anim = newAnim
        if (crate.canBePlaced(nextField._1, nextField._2)) {
          dropPreview.visible = true
          dropPreview.position.copy(map.mapPosToVec(nextField))
          dropPreview.position.setY(-.5)
        }
        move(direction.clone().multiplyScalar(state.speed * deltaTime))
        if (isHarmedByMonster(positionOnMap)) {
          setState(Dead())
        }
        mesh.scale.setY(1.0 + Math.sin(anim * 2) * .2)
        mesh.scale.setZ(1.0 - Math.sin(anim * 2 + .3) * .05)
      case s @ Dead() =>
        s.deathAnim = Math.max(s.deathAnim - .005 * deltaTime, .66)
        mesh.position.set(position.x, position.y - 1.9 + s.deathAnim * 1.9, position.z)
        mesh.scale.setY((1.0 + Math.sin(anim * 2) * .1) * s.deathAnim + .1)
        mesh.scale.setX((1.0 - Math.sin(anim * 2 + .2) * .05) / s.deathAnim)
        mesh.scale.setZ(mesh.scale.x)
      case s @ Frozen(byCrate) =>
        mesh.position.copy(byCrate.getPosition)
      case Carrying(_) =>
        // this cannot actually happen
    }

    updateShadow()
  }

  private def isHarmedByMonster(xz: (Int, Int)) = {
    map.getObjectsAt(xz).exists {
      case monster: Monster =>
        monster.sizeLevel > 1
      case _ => false
    }
  }

  /** Transforms the player in the plane. (The y component of the Vec3 will be ignored!) */
  def move(dir: Vector3): Unit = {
    if (dir.x != 0 || dir.z != 0) {
      viewDirection = Direction.fromVec(dir)
      val newPos = map.localSlideCast(position, dir,
        bumpingDist = if (state.isInstanceOf[Carrying]) .6 else .4 )
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
    
    if (touching.isEmpty && !state.isInstanceOf[Carrying]) {
      val newTouchObj = neighboringObjects.collectFirst { case c: Crate => c }

      newTouchObj.foreach { c =>
        if (!c.kind.isInstanceOf[CrateData.FreezerKind]) context.audio.play("player-crate")
        touch(c)
      }
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
      case Dead() =>
        state match {
          case Carrying(c) =>
            place(c.asInstanceOf[Crate])
          case _ =>
        }
        newState
      case Spawning(ySpeed) =>
        if (ySpeed < 0) {
          setPosition(position.x, 70, position.z)
        }
        newState
      case s => s
    }
  }
}