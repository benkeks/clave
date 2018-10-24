package net.mrkeks.clave.view

import net.mrkeks.clave.game.Game
import org.scalajs.dom

class GUI() {

  private val hudContainer = dom.document.createElement("div")
  hudContainer.classList.add("hud")

  private val scoreText = dom.document.createTextNode("Score: 0") 
  hudContainer.appendChild(scoreText)

  private val pauseButton = dom.document.createElement("button")
  private val pauseButtonText = dom.document.createTextNode("Pause")
  pauseButton.appendChild(pauseButtonText)
  pauseButton.addEventListener("click", clickPause _)
  hudContainer.appendChild(pauseButton)

  dom.document.body.appendChild(hudContainer)

  var game: Option[Game] = None

  def registerGame(game: Game) {
    this.game = Some(game)
  }

  def setScore(score: Int) {
    scoreText.textContent = "Score: "+score
  }

  def clickPause(ev: org.scalajs.dom.raw.Event): Unit = {
    println("hit pause!")
    game foreach (_.togglePause())
  }

  def notifyGameState(): Unit = {
    game map (_.state) match {
      case Some(Game.Paused()) =>
        pauseButtonText.textContent = "Continue"
      case _ =>
        pauseButtonText.textContent = "Pause"
    }
  }
}