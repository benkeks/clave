package net.mrkeks.clave.game.objects

import net.mrkeks.clave.game.Game
import net.mrkeks.clave.game.abstracts.PositionedObject
import net.mrkeks.clave.game.abstracts.GameObject
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.view.DrawingContext

object Meta {
  abstract sealed class State
  case class Waiting() extends State
  case class Active() extends State
}

class Meta(protected val map: GameMap)
  extends GameObject with PositionedObject {

  var state: Meta.State = Meta.Waiting()

  private var game: Option[Game] = None
  
  def init(context: DrawingContext): Unit = {
  }

  def clear(context: DrawingContext): Unit = {
  }

  def update(deltaTime: Double): Unit = {
    state match {
      case Meta.Waiting() =>
        for {
          g <- game
          p <- g.player
          if p.getPosition.y <= 10
        } {
          state = Meta.Active()
          game.foreach(_.showMeta(info))
        }
      case Meta.Active() =>
        markForDeletion()
    }
  }

  def registerGame(game: Game): Unit = {
    this.game = Some(game)
  }

}
