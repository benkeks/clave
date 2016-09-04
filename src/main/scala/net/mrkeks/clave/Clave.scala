package net.mrkeks.clave


import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom
import net.mrkeks.clave.game.GameObject
import net.mrkeks.clave.game.Player
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.map.Maps
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.game.PlayerControl
import net.mrkeks.clave.view.Input
import net.mrkeks.clave.map.MapData
import net.mrkeks.clave.game.Crate
import net.mrkeks.clave.game.Monster
import net.mrkeks.clave.game.Game

@JSExport
object Clave {
  
  val context = new DrawingContext()
  
  val input = new Input()
  
  val game = new Game(context, input)
  
  @JSExport
  def main(): Unit = {
    game.loadLevel(Maps.level0)
    
    dom.window.setInterval(() => game.update(), 20)
  }
}
