package net.mrkeks.clave.editor

import net.mrkeks.clave.util.TimeManagement
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.view.Input
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.map.MapData
import net.mrkeks.clave.view.GUI
import net.mrkeks.clave.map.Level
import net.mrkeks.clave.game.GameObjectManagement
import net.mrkeks.clave.game.GameLevelLoader
import net.mrkeks.clave.game.objects.Crate
import net.mrkeks.clave.game.objects.Gate
import net.mrkeks.clave.game.objects.Trigger
import net.mrkeks.clave.game.objects.TriggerGroup
import net.mrkeks.clave.game.characters.Player
import net.mrkeks.clave.game.characters.PlayerData
import net.mrkeks.clave.editor.tools.AbstractEditorTool
import net.mrkeks.clave.editor.tools.ObjectPlacer

class Editor(val context: DrawingContext, val input: Input, val gui: GUI)
  extends GameObjectManagement with GameLevelLoader with TimeManagement {
  
  var levelId: Int = 0
  
  var player: Player = null

  var map: GameMap = null

  val editorInput = new EditorInput(context)

  val editorTools = List[AbstractEditorTool](
    new ObjectPlacer(() => new Crate(map))
  )

  var currentEditorTool = editorTools(0)
  
  def update(timeStamp: Double): Unit = {

    input.update(timeStamp)

    gameObjects.foreach(_.update(0))

    context.render()

    updateTime(timeStamp)

    editorInput.update(timeStamp, this)
  }

}