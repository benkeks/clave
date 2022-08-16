package net.mrkeks.clave.editor

import net.mrkeks.clave.util.TimeManagement
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.view.Input
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.map.MapData
import net.mrkeks.clave.view.GUI
import net.mrkeks.clave.map.LevelDownloader
import net.mrkeks.clave.game.abstracts.GameObjectManagement
import net.mrkeks.clave.game.GameLevelLoader
import net.mrkeks.clave.game.objects
import net.mrkeks.clave.game.characters
import net.mrkeks.clave.editor.tools.AbstractEditorTool
import net.mrkeks.clave.editor.tools.ObjectPlacer

class Editor(val context: DrawingContext, val input: Input, val gui: EditorGUI, val levelDownloader: LevelDownloader)
  extends GameObjectManagement with GameLevelLoader with TimeManagement {
  
  var levelId: Int = 0
  
  var player: Option[characters.Player] = None

  var map: GameMap = null

  val editorInput = new EditorInput(this)

  val editorTools = List[AbstractEditorTool](
    new ObjectPlacer("Crate", () => new objects.Crate(map)),
    new ObjectPlacer("Monster", () => new characters.Monster(map))
  )

  gui.registerTools(editorTools, setTool _)

  var currentEditorTool = editorTools(1)

  context.particleSystem.registerParticleType("gfx/dust.png", "dust", maxAmount = 0)
  context.particleSystem.registerParticleType("gfx/dust.png", "point", maxAmount = 0)
  context.particleSystem.registerParticleType("gfx/shadow.gif", "spark", maxAmount = 0)

  def update(timeStamp: Double): Unit = {

    input.update(timeStamp)

    gameObjects.foreach(_.update(0))

    context.render()

    updateTime(timeStamp)

    editorInput.update(timeStamp)

    removeAllMarkedForDeletion()
  }

  def setTool(name: String): Unit = {
    for (t <- editorTools.find(_.name == name)) {
      currentEditorTool.deactivate()
      currentEditorTool = t
    }
  }

}