package net.mrkeks.clave.game.objects

import org.denigma.threejs.BoxGeometry
import org.denigma.threejs.Mesh
import org.denigma.threejs.MeshLambertMaterial
import org.denigma.threejs.{Vector3, Vector4}

import net.mrkeks.clave.game.abstracts.GameObject
import net.mrkeks.clave.game.abstracts.PositionedObject
import net.mrkeks.clave.game.characters.Monster
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.view.ParticleSystem
object Gate {
  private val material = new MeshLambertMaterial()
  material.color.setHex(0x8899aa)
  
  private val box = new BoxGeometry(.99, 1.0, .99)
  
  def clear(): Unit = {
    material.dispose()
    box.dispose()
  }
}

class Gate(protected val map: GameMap)
  extends GameObject with PositionedObject with GateData {
  
  import GateData._
  
  val mesh = new Mesh(Gate.box, Gate.material)
  var context: DrawingContext = null
  var animY = 0.0
  
  def init(context: DrawingContext): Unit = {
    context.scene.add(mesh)
    this.context = context
  }
  
  def clear(context: DrawingContext): Unit = {
    context.scene.remove(mesh)
  }
  
  def update(deltaTime: Double): Unit = {
    state match {
      case Open() =>
        animY = Math.max(animY - .01 * deltaTime, -.99)
      case Closing() =>
        if (animY < 0.0) {
          animY = animY + (.013 + Math.sin(position.x*.5)*.01) * deltaTime
        } else {
          setState(Closed())
          context.particleSystem.burst("dust", 10, ParticleSystem.BurstKind.Box,
            new Vector3(position.x-.25, position.y - .4, position.z-.25), new Vector3(position.x+.25, position.y - .3, position.z+.25),
            new Vector3(-.0025,.0,-.0025), new Vector3(.0025, .0, .0025), new Vector4(.6, .6, .6, .5), new Vector4(.8, .8, .8, .7), .1, .4)
          updatePositionOnMap()
        }
      case Closed() =>
        animY = 0.0
    }
    mesh.position.set(position.x, animY, position.z)
  }
  
  def open(): Unit = {
    if (state.isInstanceOf[Closed]) {
      context.particleSystem.burst("dust", 5, ParticleSystem.BurstKind.Box,
        new Vector3(position.x-.25, position.y - .6, position.z-.25), new Vector3(position.x+.25, position.y - .3, position.z+.25),
        new Vector3(-.0025,.0,-.0025), new Vector3(.0025, .0, .0025), new Vector4(.4, .4, .4, .5), new Vector4(.5, .5, .5, .7), .1, .4)
        updatePositionOnMap()
    }
    setState(Open())
    updatePositionOnMap()
  }
  
  def close(): Unit = {
    if (state.isInstanceOf[Open]) {
      setState(Closing())
      updatePositionOnMap()
    }
  }
  
  def setState(newState: State): Unit = {
    if (state != newState) {
      newState match {
        case Closed() =>
        case Closing() =>
          context.audio.play("barrier-activates")
        case Open() =>
          context.audio.play("barrier-deactivates")
      }
    }
    state = newState
  }
}