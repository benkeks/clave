package net.mrkeks.clave

import scala.scalajs.js.Any.fromFunction0
import scala.scalajs.js.annotation.{JSExportTopLevel, JSExport}

import org.scalajs.dom
import net.mrkeks.clave.game.Game
import net.mrkeks.clave.map.Level
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.view.GUI
import net.mrkeks.clave.view.Input
import net.mrkeks.clave.editor.Editor


@JSExportTopLevel("Clave")
object Clave {
  
  class Config(val startLevel: Int = 0, val editor: Boolean = false)

  @JSExport
  def main(): Unit = {
    
    val configuration = loadConfig()

    val context = new DrawingContext()
    
    val gui = new GUI()
    
    val input = new Input()

    if (configuration.editor) {

      val editor: Editor = new Editor(context, input, gui)

      editor.loadLevel(configuration.startLevel)
      
      def update(timeStamp: Double): Unit = {
        editor.update(timeStamp)
        dom.window.requestAnimationFrame(update _)
      }
  
      dom.window.requestAnimationFrame(update _)

    } else {

      val game: Game = new Game(context, input, gui)

      game.loadLevel(configuration.startLevel)
      game.setState(Game.Running())
      
      def update(timeStamp: Double): Unit = {
        game.update(timeStamp)
        dom.window.requestAnimationFrame(update _)
      }
  
      dom.window.requestAnimationFrame(update _)
    }
  }

  def loadConfig() = {

    val cfgs = { for {
      assignment <- dom.window.location.hash.drop(1).split('&')
      kv = assignment.split('=')
      if kv.length == 2
    } yield (kv(0), kv(1)) }.toMap

    new Config(
      startLevel = cfgs.get("level").flatMap(_.toIntOption).getOrElse(0),
      editor = cfgs.get("editor").map(_ == "true").getOrElse(false)
    )
  }
}
