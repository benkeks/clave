package net.mrkeks.clave.editor.tools

import net.mrkeks.clave.game.abstracts.GameObjectManagement

import org.denigma.threejs

trait AbstractEditorTool {

  val name: String

  def deactivate(): Unit

  def previewTool(intersection: threejs.Intersection, gameObjectManagement: GameObjectManagement): AbstractEditorTool.Result

  def runTool(intersection: threejs.Intersection, gameObjectManagement: GameObjectManagement): AbstractEditorTool.Result

}

object AbstractEditorTool {

  abstract sealed class Result()
  case class Success() extends Result()
  case class Fail() extends Result()

}