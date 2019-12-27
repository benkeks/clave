package net.mrkeks.clave

import scala.scalajs.js.Any.fromFunction0
import scala.scalajs.js.annotation.{JSExportTopLevel, JSExport}

import org.scalajs.dom
import net.mrkeks.clave.game.Game
import net.mrkeks.clave.map.Level
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.view.GUI
import net.mrkeks.clave.view.Input


@JSExportTopLevel("Clave")
object Clave {
  
  @JSExport
  def main(): Unit = {

    val context = new DrawingContext()
    
    val gui = new GUI()
    
    val input = new Input()
    
    val game: Game = new Game(context, input, gui)

    game.loadLevel(0)
    game.setState(Game.Running())
    
    def tick(timeStamp: Double) {
      game.update(timeStamp)
      dom.window.requestAnimationFrame(tick _)
    }

    dom.window.requestAnimationFrame(tick _)
  }
}
