package net.mrkeks.clave.editor.tools

import net.mrkeks.clave.game.{GameObject, PositionedObject}
import net.mrkeks.clave.game.GameObjectManagement

import org.denigma.threejs

class ObjectPlacer(factory: () => GameObject with PositionedObject) extends AbstractEditorTool {
  
  def runTool(intersection: threejs.Intersection, gameObjectManagement: GameObjectManagement): AbstractEditorTool.Result = {
    
    val newObject = factory()

    gameObjectManagement.add(newObject)
    newObject.setPosition(intersection.point.x, 0, intersection.point.z)

    AbstractEditorTool.Success()
  }
}