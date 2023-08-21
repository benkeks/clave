package net.mrkeks.clave

import scala.scalajs.js.Any.fromFunction0
import scala.scalajs.js.annotation.{JSExportTopLevel, JSExport}
import org.scalajs.dom

import net.mrkeks.clave.game.Game
import net.mrkeks.clave.map.Level
import net.mrkeks.clave.map.LevelDownloader
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.view.GUI
import net.mrkeks.clave.view.Input
import net.mrkeks.clave.editor.Editor
import net.mrkeks.clave.editor.EditorGUI
import net.mrkeks.clave.game.abstracts.ObjectShadow


@JSExportTopLevel("Clave")
object Clave {

  class Config(val startLevel: Int = 0, val editor: Boolean = false)

  val DevMode: Boolean = true

  @JSExport
  def main(): Unit = {

    val configuration = loadConfig()

    val serviceWorkerManager = dom.window.navigator.serviceWorker;
    if (serviceWorkerManager != null) {
      serviceWorkerManager.register("/service-worker.js")
    }

    val context = new DrawingContext()
    ObjectShadow.init(context)

    val input = new Input()

    val levelDownloader = new LevelDownloader()
    levelDownloader.downloadWorld("levels/clave.world") {

      if (configuration.editor) {

        val gui = new EditorGUI()

        val editor: Editor = new Editor(context, input, gui, levelDownloader)
        val firstLevelId = levelDownloader.levelList(configuration.startLevel)
        editor.loadLevelById(firstLevelId)

        def update(timeStamp: Double): Unit = {
          editor.update(timeStamp)
          dom.window.requestAnimationFrame(update _)
        }

        dom.window.requestAnimationFrame(update _)

      } else {

        val gui = new GUI()

        val game: Game = new Game(context, input, gui, levelDownloader)
        val initialLevelId = levelDownloader.getLevelIdByNum(configuration.startLevel)
        game.unlockLevel(initialLevelId)
        if (DevMode) levelDownloader.levelList.foreach(game.unlockLevel(_))
        game.loadLevelById("__titleScreen__")
        game.setState(Game.StartUp(0))

        def update(timeStamp: Double): Unit = {
          game.update(timeStamp)
          gui.update(timeStamp)
          dom.window.requestAnimationFrame(update _)
        }

        dom.window.requestAnimationFrame(update _)
      }
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
