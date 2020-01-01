package net.mrkeks.clave.editor.tools

import net.mrkeks.clave.game.{GameObject, PlaceableObject}
import net.mrkeks.clave.game.GameObjectManagement

import org.denigma.threejs

class ObjectPlacer[T <: GameObject with PlaceableObject](factory: () => T) extends AbstractEditorTool {
  
  private var newObject: Option[T] = None

  def previewTool(intersection: threejs.Intersection, gameObjectManagement: GameObjectManagement): AbstractEditorTool.Result = {
    
    createObjectIfNeeded(gameObjectManagement)

    if (newObject.map(_.place(intersection.point.x.round.toInt, intersection.point.z.round.toInt)).getOrElse(false)) {
      AbstractEditorTool.Success()
    } else {
      AbstractEditorTool.Fail()
    }
  }

  def runTool(intersection: threejs.Intersection, gameObjectManagement: GameObjectManagement): AbstractEditorTool.Result = {
    
    newObject.foreach(_.isPreview = false)

    previewTool(intersection, gameObjectManagement) match {
      case s @ AbstractEditorTool.Success() =>
        newObject = None
        s
      case f @ AbstractEditorTool.Fail() =>
        newObject.foreach(_.isPreview = true)
        f
    }

  }

  private def createObjectIfNeeded(gameObjectManagement: GameObjectManagement): Unit = {
    if (newObject.isEmpty) {
      val creation = factory()
      creation.isPreview = true
      gameObjectManagement.add(creation)
      newObject = Some(creation)
    }
  }
}