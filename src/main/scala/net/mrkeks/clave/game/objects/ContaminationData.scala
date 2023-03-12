package net.mrkeks.clave.game.objects

import net.mrkeks.clave.game.abstracts.PositionedObjectData
import org.denigma.threejs.Vector3

trait ContaminationData
  extends PositionedObjectData {

  var timeToLive: Double = 0
  var creationProgress: Double = 0
  var visualPosition = new Vector3()

  def isDangerous() = {
    creationProgress > 1.0 && timeToLive > .8
  }
}