package net.mrkeks.clave

import scala.scalajs.js.Any.fromFunction0
import scala.scalajs.js.annotation.JSExportTopLevel

import org.scalajs.dom
import net.mrkeks.clave.game.Game
import net.mrkeks.clave.map.Level
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.view.GUI
import net.mrkeks.clave.view.Input

@JSExportTopLevel("ClaveEditor")
object ClaveEditor {
  
  def main(): Unit = {

    val context = new DrawingContext()
  
    val gui = new GUI()
    
    val input = new Input()
    
    val game: Game = new Game(context, input, gui)

    game.loadLevel(2)
    game.setState(Game.Running())
    
    //dom.window.setInterval(() => game.update(), 20)
  }
}