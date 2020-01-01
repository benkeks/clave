package net.mrkeks.clave.editor.tools

import net.mrkeks.clave.game.GameObjectManagement

import org.denigma.threejs

trait AbstractEditorTool {

  def previewTool(intersection: threejs.Intersection, gameObjectManagement: GameObjectManagement): AbstractEditorTool.Result

  def runTool(intersection: threejs.Intersection, gameObjectManagement: GameObjectManagement): AbstractEditorTool.Result

}

object AbstractEditorTool {

  abstract sealed class Result()
  case class Success() extends Result()
  case class Fail() extends Result()

}