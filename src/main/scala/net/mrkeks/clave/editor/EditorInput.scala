package net.mrkeks.clave.editor

import net.mrkeks.clave.view.DrawingContext

import org.scalajs.dom

import org.denigma.threejs

class EditorInput(editor: Editor) {

  private val context = editor.context
  
  private val pointer = new threejs.Vector2()

  private val raycaster = new threejs.Raycaster()

  private var currentIntersection: Option[threejs.Intersection] = None

  dom.window.onmousemove = { e: dom.MouseEvent =>
    pointer.x = 2.0 * (e.clientX / context.width) - 1.0
    pointer.y = 1.0 - 2.0 * (e.clientY / context.height)
  }

  dom.window.onclick = { e: dom.MouseEvent =>
    currentIntersection.foreach(editor.currentEditorTool.runTool(_, editor))
  }
  
  def update(timeStamp: Double): Unit = {
    raycaster.setFromCamera(pointer, context.camera)
    currentIntersection = raycaster.intersectObjects(context.scene.children).headOption
    currentIntersection.foreach(editor.currentEditorTool.previewTool(_, editor))
  }

}