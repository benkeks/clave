package net.mrkeks.clave.game.abstracts

import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.view.ParticleSystem
import net.mrkeks.clave.util.Mathf

import org.denigma.threejs.{Vector3, Vector4}
import net.mrkeks.clave.game.objects.CrateData

trait FreezableObject {
  self: PositionedObject =>

  private var freezeProgress = 0.0

  def updateFreezable(deltaTime: Double, context: DrawingContext) = {
    freezeProgress = Mathf.clamp(freezeProgress - deltaTime, 0, 100)
    if (freezeProgress > 0.01) {
      val offset = new Vector3(-.5 + Math.random(), -1.0 + .5*Math.random(), -.5 + Math.random())
      val start = position.clone()
      start.x += -.5 + Math.random()
      start.y += -1.0 + .5 * Math.random()
      start.z += -.5 + Math.random()
      val dir = new Vector3(-.001 + .002 * Math.random(), .001 + .001 * Math.random(), -.001 + .002 * Math.random())
      val color = new Vector4(.2, .6, 1.0, .7)
      context.particleSystem.burst("spark", 1, ParticleSystem.BurstKind.Box,
        start, start, dir, dir, color, color, .1, .2)
    }
  }

  def doFreeze(time: Double, source: CrateData): Boolean = {
    freezeProgress += 1.1 * time
    if (freezeProgress > 100) {
      freezeComplete(source)
    } else {
      false
    }
  }

  /** returns true if freeze could actually go through */
  def freezeComplete(byCrate: CrateData): Boolean
}
