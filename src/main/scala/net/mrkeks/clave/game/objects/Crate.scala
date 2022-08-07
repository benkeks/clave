package net.mrkeks.clave.game.objects

import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.view.ParticleSystem
import net.mrkeks.clave.game.PlaceableObject
import net.mrkeks.clave.game.GameObject
import net.mrkeks.clave.game.PositionedObjectData
import net.mrkeks.clave.game.characters.Monster
import net.mrkeks.clave.game.characters.Player
import net.mrkeks.clave.map.MapData
import net.mrkeks.clave.map.GameMap

import org.denigma.threejs.MeshLambertMaterial
import org.denigma.threejs.BoxGeometry
import org.denigma.threejs.Mesh
import org.denigma.threejs.BoxHelper
import org.denigma.threejs.Material
import org.denigma.threejs.Texture
import org.denigma.threejs.{Vector3, Vector4}

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
      m.color.setHex(0xdddd99)
      m
    }
  )

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

  var context: DrawingContext = null

  def init(context: DrawingContext): Unit = {
    mesh.rotateY(.1 - .2 * Math.random())
    context.scene.add(mesh)
    this.context = context
  }
  
  def clear(context: DrawingContext): Unit = {
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
    mesh.position.copy(position)
  }
  
  def pickup(player: Player): Unit = {
    state = Carried(player)

    context.particleSystem.burst("dust", 10, ParticleSystem.BurstKind.Box,
      new Vector3(position.x-.25, position.y - .4, position.z-.25), new Vector3(position.x+.25, position.y - .3, position.z+.25),
      new Vector3(-.003,.0,-.003), new Vector3(.003, .0, .003), new Vector4(.4, .4, .4, .5), new Vector4(.8, .8, .8, .7), .4, .6)

    // temporally move to fictional position in order to remove location from game map
    position.setZ(-10)
    updatePositionOnMap()
  }
  
  override def place(x: Int, z: Int) = {
    if (canBePlaced(x, z)) {
      setPosition(x, 0, z)
      context.particleSystem.burst("dust", 10, ParticleSystem.BurstKind.Box,
        new Vector3(position.x-.25, position.y - .4, position.z-.25), new Vector3(position.x+.25, position.y - .3, position.z+.25),
        new Vector3(-.003,.0,-.003), new Vector3(.003, .0, .003), new Vector4(.4, .4, .4, .5), new Vector4(.8, .8, .8, .7), .5, .9)
      state = Standing()
      true
    } else {
      false
    }
  }
  
  override def canBePlaced(x: Int, z: Int) = (
    !map.intersectsLevel(x, z, considerObstacles = false)
      && !map.getObjectsAt((x,z)).exists {
             case _: Monster | _: Gate => true
             case _ => false
    })
}