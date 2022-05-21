package net.mrkeks.clave.view

import net.mrkeks.clave.game.Game
import org.scalajs.dom
import net.mrkeks.clave.util.TimeManagement

class GUI() extends TimeManagement {

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
  private var popupText = ""

  dom.document.body.appendChild(hudContainer)

  var game: Option[Game] = None

  def registerGame(game: Game): Unit = {
    this.game = Some(game)
  }

  def setScore(score: Int): Unit = {
    scoreText.textContent = "Score: "+score
  }

  /**
    * @param text popup text to be displayed
    * @param time if set, remove the the popup after `time` milliseconds
    */
  def setPopup(text: String, time: Double = 0): Unit = {
    if (text == "") {
      popup.classList.remove("visible")
    } else {
      popup.innerHTML = text
      popup.classList.add("visible")
    }
    popupText = text
    if (time != 0) {
      schedule(lastFrameTime + time) { () =>
        if (popupText == text) {
          setPopup("")
        }
      }
    }
  }

  def update(timeStamp: Double): Unit = {
    updateTime(timeStamp)
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