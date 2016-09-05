package net.mrkeks.clave

import scala.scalajs.js.Any.fromFunction0
import scala.scalajs.js.annotation.JSExport

import org.scalajs.dom

import net.mrkeks.clave.game.Game
import net.mrkeks.clave.map.Maps
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.view.Input

@JSExport
object Clave {
  
  val context = new DrawingContext()
  
  val input = new Input()
  
  val game = new Game(context, input)
  
  @JSExport
  def main(): Unit = {
    game.loadLevel(Maps.level1)
    
    dom.window.setInterval(() => game.update(), 20)
  }
}
