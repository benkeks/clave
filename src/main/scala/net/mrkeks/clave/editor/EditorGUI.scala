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

  private val shareButtons = dom.document.createElement("div")
  shareButtons.classList.add("dropdown")
  hudContainer.appendChild(shareButtons)
  private val shareButton = dom.document.createElement("button")
  shareButton.id = "share-button"
  shareButton.classList.add("btn")
  shareButton.classList.add("btn-secondary")
  shareButton.classList.add("dropdown-toggle")
  shareButton.setAttribute("type", "button")
  shareButton.setAttribute("data-toggle", "dropdown")
  shareButton.setAttribute("aria-haspopup", "true")
  shareButton.setAttribute("aria-expanded", "false")
  shareButton.innerText = "Share/Play"
  shareButtons.appendChild(shareButton)
  private val shareDiv = dom.document.createElement("div")
  shareDiv.classList.add("dropdown-menu")
  shareDiv.setAttribute("aria-labelledby", "share-button")
  shareButtons.appendChild(shareDiv)
  shareButton.addEventListener("click", (ev: org.scalajs.dom.Event) => renderShare())

  def registerTools(tools: List[AbstractEditorTool], selectionCallback: String => Unit) = {
    for (t <- tools) {
      val button = dom.document.createElement("button")
      button.classList.add("btn")
      button.classList.add("btn-secondary")
      button.setAttribute("type", "button")
      button.innerText = t.name
      button.addEventListener("click", (ev: org.scalajs.dom.Event) => selectionCallback(t.name))
      toolButtons.appendChild(button)
    }
  }

  def renderShare() = {
    shareDiv.innerHTML = """
  <h6 class="dropdown-header">Dropdown header</h6>
     <input type="text" class="form-control" id="share-url" value="ShareURL">
    """
  }
/*
  private val toolButtons = dom.document.createElement("div")

  <button type="button" class="btn btn-secondary">Left</button>
  <button type="button" class="btn btn-secondary">Middle</button>
  <button type="button" class="btn btn-secondary">Right</button>
  
</div>*/

}