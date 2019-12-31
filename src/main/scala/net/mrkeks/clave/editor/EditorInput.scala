package net.mrkeks.clave.editor

import net.mrkeks.clave.view.DrawingContext

import org.scalajs.dom

import org.denigma.threejs

class EditorInput(val context: DrawingContext) {
  
  val pointer = new threejs.Vector2()

  val raycaster = new threejs.Raycaster()

  dom.window.onmousemove = { e: dom.MouseEvent =>
    pointer.x = 2.0 * (e.clientX / context.width) - 1.0
    pointer.y = 1.0 - 2.0 * (e.clientY / context.height)
  }
  
  def update(timeStamp: Double, editor: Editor): Unit = {
    raycaster.setFromCamera(pointer, context.camera)
    val intersects = raycaster.intersectObjects(context.scene.children)
    intersects.headOption.foreach(editor.currentEditorTool.runTool(_, editor))
  }

}