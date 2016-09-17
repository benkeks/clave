package net.mrkeks.clave

import scala.scalajs.js.Any.fromFunction0
import scala.scalajs.js.annotation.JSExport

import org.scalajs.dom

import net.mrkeks.clave.game.Game
import net.mrkeks.clave.map.Level
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.view.GUI
import net.mrkeks.clave.view.Input

@JSExport
object ClaveEditor {
  
  val context = new DrawingContext()
  
  val gui = new GUI()
  
  val input = new Input()
  
  val game = new Game(context, input, gui)
  
  @JSExport
  def main(): Unit = {
    game.loadLevel(2)
    game.setState(game.Running())
    
    dom.window.setInterval(() => game.update(), 20)
  }
}