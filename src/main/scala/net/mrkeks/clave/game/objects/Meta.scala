package net.mrkeks.clave.game.objects

import net.mrkeks.clave.game.abstracts.PositionedObject
import net.mrkeks.clave.game.abstracts.GameObject
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.view.DrawingContext

class Meta(protected val map: GameMap)
  extends GameObject with PositionedObject {

  def init(context: DrawingContext): Unit = {
  }

  def clear(context: DrawingContext): Unit = {
  }

  def update(deltaTime: Double): Unit = {
  }

}
