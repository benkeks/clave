package net.mrkeks.clave.game.objects

import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.view.ParticleSystem
import net.mrkeks.clave.game.abstracts._
import net.mrkeks.clave.game.characters.Monster
import net.mrkeks.clave.game.characters.Player
import net.mrkeks.clave.map.MapData
import net.mrkeks.clave.map.GameMap

import org.denigma.threejs
import org.denigma.threejs.MeshLambertMaterial
import org.denigma.threejs.BoxGeometry
import org.denigma.threejs.Mesh
import org.denigma.threejs.Material
import org.denigma.threejs.Texture
import org.denigma.threejs.{Vector3, Vector4}
import net.mrkeks.clave.util.Mathf

object Crate {
  private val materials = Map[CrateData.Kind, Material](
    CrateData.DefaultKind -> {
      val m = new MeshLambertMaterial()
      m.color.setHex(0xdddd99)
      m
    },
    CrateData.PlayerLikeKind -> {
      val m = new MeshLambertMaterial()
      DrawingContext.textureLoader.load("gfx/player_monster_texture.png", { tex: Texture =>
        m.map = tex
        m.needsUpdate = true
      })
      m.color.setHex(0xffccbb)
      m.emissive.setHex(0x332211)
      m
    },
    CrateData.FreezerKind(None) -> {
      val m = new threejs.MeshStandardMaterial()
      m.color.setHex(0xccddff)
      m.transparent = true
      m.opacity = .8
      m
    }
  )

  private val highlightBox = {
    val geometry = new BoxGeometry(.99, .99, .99)
    val material = new MeshLambertMaterial()
    material.emissive.setHex(0xaa3344)
    material.side = threejs.THREE.BackSide
    new Mesh(geometry, material)
  }

  private val box = new BoxGeometry(.95, .95, .95)
  
  def clear(): Unit = {
    materials.values.foreach(_.dispose())
    box.dispose()
  }
}

class Crate(
    protected val map: GameMap,
    val kind: CrateData.Kind = CrateData.DefaultKind)
  extends GameObject with PlaceableObject with CrateData {

  import CrateData._

  val mesh = new Mesh(Crate.box, Crate.materials(kind))

  val highlightBox = Crate.highlightBox.clone()

  var context: DrawingContext = null

  def init(context: DrawingContext): Unit = {
    mesh.rotateY(.1 - .2 * Math.random())
    context.scene.add(mesh)
    context.scene.add(highlightBox)
    highlightBox.visible = false
    highlightBox.parent = mesh
    this.context = context
  }
  
  def clear(context: DrawingContext): Unit = {
    context.scene.remove(highlightBox)
    context.scene.remove(mesh)
  }
  
  def update(deltaTime: Double): Unit = {
    state match {
      case Standing() =>
        position.setY(if (touching.isEmpty) -.025 else .1)
      case Carried(player) =>
        val pos = player.asInstanceOf[Player].mesh.localToWorld(new Vector3(0,.5,-.3))
        pos.y += .3
        position.copy(pos)
    }
    kind match { 
      case CrateData.PlayerLikeKind =>
        // occasional “attraction” particles 
        if (Math.random() > .98) {
          val offset = new Vector3(-1.5 + 3.0*Math.random(), -.5 + 1.5*Math.random(), -1.5 + 3.0*Math.random())
          val start = position.clone().sub(offset)
          val color = if (Math.random() > .7) new Vector4(.9, .9, .9, .8) else new Vector4(.8, .1, .1, .7)
          offset.multiplyScalar(.0005)
          context.particleSystem.burst("spark", 1, ParticleSystem.BurstKind.Box,
            start, start, offset, offset, color, color, .0, .1)
        }
      case k: CrateData.FreezerKind if k.frozenMonster.isEmpty =>
        mesh.scale.y = .1
        position.y -= .4
        map.getObjectsAt(positionOnMap).collectFirst {
          case fm: FreezableObject =>
            if (!fm.isBeingFrozen()) {
              context.audio.play("freezer-activates")
            }
            if (fm.doFreeze(deltaTime, this)) {
              context.audio.play("freezer-freezes")
              k.frozenMonster = Some(fm)
              updatePositionOnMap()
            }
        }
      case k: CrateData.FreezerKind if k.frozenMonster.nonEmpty =>
        mesh.scale.y = Mathf.approach(mesh.scale.y, 1.3, deltaTime * .01)
        mesh.scale.x = 1.15
        mesh.scale.z = 1.15
      case _ =>
    }
    mesh.position.copy(position)
    highlightBox.visible = touching.nonEmpty
  }
  
  def pickup(player: Player): Boolean = {
    state = Carried(player)

    context.audio.play("crate-pickup")
    context.particleSystem.burst("dust", 10, ParticleSystem.BurstKind.Box,
      new Vector3(position.x-.25, position.y - .4, position.z-.25), new Vector3(position.x+.25, position.y - .3, position.z+.25),
      new Vector3(-.0025,.0,-.0025), new Vector3(.0025, .0, .0025), new Vector4(.4, .4, .4, .5), new Vector4(.8, .8, .8, .7), .3, .6)

    // temporally remove location from game map
    removeFromMap()
    true
  }
  
  override def place(x: Int, z: Int) = {
    if (canBePlaced(x, z) && positionOnMap != (x,z)) {
      setPosition(x, 0, z)
      kind match {
        case DefaultKind => context.audio.play("crate-place")
        case FreezerKind(frozenMonster) => context.audio.play("crate-place-freeze")
        case PlayerLikeKind => context.audio.play("crate-place-red")
      }
      context.particleSystem.burst("dust", 10, ParticleSystem.BurstKind.Box,
         new Vector3(position.x-.25, position.y - .4, position.z-.25), new Vector3(position.x+.25, position.y - .3, position.z+.25),
         new Vector3(-.0025,.0,-.0025), new Vector3(.0025, .0, .0025), new Vector4(.4, .4, .4, .5), new Vector4(.8, .8, .8, .7), .4, .7)
      state = Standing()
      true
    } else {
      false
    }
  }
  
  override def canBePlaced(x: Int, z: Int) = (
    !map.intersectsLevel(x, z, considerObstacles = false)
      && !map.getObjectsAt((x,z)).exists {
             case _: Monster | _: Gate | _: Crate => true
             case _ => false
    })
}