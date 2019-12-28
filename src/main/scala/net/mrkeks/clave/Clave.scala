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
  
  class Config(startLevel: Int = 0)

  @JSExport
  def main(): Unit = {
    
    //val configuration = loadConfig()

    val context = new DrawingContext()
    
    val gui = new GUI()
    
    val input = new Input()
    
    val game: Game = new Game(context, input, gui)

    game.loadLevel(0)
    game.setState(Game.Running())
    
    def tick(timeStamp: Double): Unit = {
      game.update(timeStamp)
      dom.window.requestAnimationFrame(tick _)
    }

    dom.window.requestAnimationFrame(tick _)
  }

  def loadConfig() = {

    /*val cfgs = { for {
      assignment <- dom.window.location.hash.drop(1).split('&')
      kv = assignment.split('=')
      if kv.length == 2
    } yield (kv(0), kv(1)) }.toMap

    cfgs.get("level").map(_.toInt)

    new Config(
      startLevel = cfgs.
    )*/
  }
}
