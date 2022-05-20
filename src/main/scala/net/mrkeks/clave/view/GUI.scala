package net.mrkeks.clave.view

import net.mrkeks.clave.game.Game
import org.scalajs.dom

class GUI() {

  private val hudContainer = dom.document.createElement("div")
  hudContainer.id = "hud"

  private val scoreTextNode = dom.document.createElement("p")
  scoreTextNode.classList.add("score")
  hudContainer.appendChild(scoreTextNode)
  private val scoreText = dom.document.createTextNode("Score: 0")
  scoreTextNode.appendChild(scoreText)

  private val pauseButton = dom.document.createElement("button").asInstanceOf[dom.raw.HTMLElement]
  private val pauseButtonText = dom.document.createTextNode("Pause")
  pauseButton.classList.add("btn")
  pauseButton.classList.add("btn-secondary")
  pauseButton.appendChild(pauseButtonText)
  pauseButton.addEventListener("click", clickPause _)
  hudContainer.appendChild(pauseButton)

  private val overlay = dom.document.createElement("div")
  overlay.id = "overlay"
  hudContainer.appendChild(overlay)

  private val popup = dom.document.createElement("div")
  popup.id = "popup"
  hudContainer.appendChild(popup)

  dom.document.body.appendChild(hudContainer)

  var game: Option[Game] = None

  def registerGame(game: Game): Unit = {
    this.game = Some(game)
  }

  def setScore(score: Int): Unit = {
    scoreText.textContent = "Score: "+score
  }

  def setPopup(text: String): Unit = {
    popup.innerHTML = text
    if (text == "") {
      popup.classList.remove("visible")
    } else {
      popup.classList.add("visible")
    }
  }

  def clickPause(ev: org.scalajs.dom.raw.Event): Unit = {
    game foreach (_.togglePause())
    pauseButton.blur()
  }

  def notifyGameState(): Unit = {
    pauseButtonText.textContent = "Pause"
    game map (_.state) match {
      case Some(Game.Paused()) =>
        pauseButtonText.textContent = "Continue"
      case Some(Game.Continuing()) =>
        overlay.classList.remove("scene-fadein")
        overlay.classList.add("scene-fadeout")
      case _ =>
        overlay.classList.remove("scene-fadeout")
        overlay.classList.add("scene-fadein")
    }
  }
}