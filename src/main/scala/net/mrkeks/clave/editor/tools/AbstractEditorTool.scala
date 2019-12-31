package net.mrkeks.clave.editor.tools

import net.mrkeks.clave.game.GameObjectManagement

import org.denigma.threejs

trait AbstractEditorTool {

  def runTool(intersection: threejs.Intersection, gameObjectManagement: GameObjectManagement): AbstractEditorTool.Result

}

object AbstractEditorTool {

  abstract sealed class Result()
  case class Success() extends Result()

}