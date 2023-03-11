package net.mrkeks.clave.game.objects

import net.mrkeks.clave.game.abstracts.PositionedObjectData

trait ContaminationData
  extends PositionedObjectData {

  var timeToLive: Double = 0
  var creationProgress: Double = 0

  def isDangerous() = {
    creationProgress > 1.0 && timeToLive > .5
  }
}