package net.mrkeks.clave.editor

import org.scalajs.dom
import net.mrkeks.clave.editor.tools.AbstractEditorTool

class EditorGUI() {

  private val hudContainer = dom.document.createElement("div")
  hudContainer.id = "hud"
  hudContainer.classList.add("editor")
  dom.document.body.appendChild(hudContainer)

  private val toolButtons = dom.document.createElement("div")
  toolButtons.classList.add("btn-group")
  toolButtons.setAttribute("role", "group")
  hudContainer.appendChild(toolButtons)

  def registerTools(tools: List[AbstractEditorTool], selectionCallback: String => Unit) = {
    for (t <- tools) {
      val button = dom.document.createElement("button")
      button.classList.add("btn")
      button.classList.add("btn-secondary")
      button.setAttribute("type", "button")
      button.innerText = t.name
      button.addEventListener("click", (ev: org.scalajs.dom.raw.Event) => selectionCallback(t.name))
      toolButtons.appendChild(button)
    }
  }
/*
  private val toolButtons = dom.document.createElement("div")

  <button type="button" class="btn btn-secondary">Left</button>
  <button type="button" class="btn btn-secondary">Middle</button>
  <button type="button" class="btn btn-secondary">Right</button>
  
</div>*/

}