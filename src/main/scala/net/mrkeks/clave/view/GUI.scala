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

  private val switchButton = dom.document.createElement("button").asInstanceOf[dom.raw.HTMLElement]
  switchButton.classList.add("btn")
  switchButton.classList.add("btn-secondary")
  switchButton.classList.add("d-none")
  switchButton.appendChild(dom.document.createTextNode("Switch Level"))
  switchButton.addEventListener("click", clickSwitch _)
  hudContainer.appendChild(switchButton)

  private val levelList = dom.document.createElement("div")
  levelList.id = "level-list"
  hudContainer.appendChild(levelList)

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

    game.levelDownloader.levelList.foreach { levelId =>
      val level = game.levelDownloader.getLevelById(levelId).get
      val levelButton = dom.document.createElement("button").asInstanceOf[dom.raw.HTMLElement]
      levelButton.classList.add("btn")
      levelButton.classList.add("btn-secondary")
      levelButton.classList.add("level-sel")
      levelButton.appendChild(dom.document.createTextNode(level.name))
      levelButton.addEventListener("click", selectLevel(levelId) _)
      levelList.appendChild(levelButton)
    }
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

  def clickSwitch(ev: org.scalajs.dom.raw.Event): Unit = {
    game foreach (_.setState(Game.LevelScreen()))
    switchButton.blur()
  }

  def selectLevel(levelName: String)(ev: org.scalajs.dom.raw.Event): Unit = {
    game foreach { g =>
      g.switchLevelById(levelName)
      g.setState(Game.Running())
    }
    ev.target.asInstanceOf[dom.raw.HTMLElement].blur()
  }

  def notifyGameState(): Unit = {
    pauseButtonText.textContent = "Pause"
    game map (_.state) match {
      case Some(Game.LevelScreen()) =>
        levelList.classList.add("visible")
        showHide(switchButton, show = false)
      case Some(Game.Paused()) =>
        pauseButtonText.textContent = "Continue"
        levelList.classList.remove("visible")
        showHide(switchButton, show = true)
      case Some(Game.Continuing()) =>
        overlay.classList.remove("scene-fadein")
        overlay.classList.add("scene-fadeout")
        levelList.classList.remove("visible")
        showHide(switchButton, show = false)
      case _ =>
        overlay.classList.remove("scene-fadeout")
        overlay.classList.add("scene-fadein")
        levelList.classList.remove("visible")
        showHide(switchButton, show = false)
    }
  }

  private def showHide(button: dom.raw.HTMLElement, show: Boolean = true) = {
    if (show) {
      button.classList.remove("d-none")
      button.classList.add("d-block")
    } else {
      button.classList.remove("d-block")
      button.classList.add("d-none")
    }
  }
}