package net.mrkeks.clave.view

import net.mrkeks.clave.game.Game
import net.mrkeks.clave.map.LevelPreviewer
import net.mrkeks.clave.util.TimeManagement

import scala.collection.mutable.Map

import org.scalajs.dom
import scala.scalajs.js.URIUtils

class GUI() extends TimeManagement {

  private object Texts {
    val ContinueSymbol = "▶"
    val PauseSymbol = "☰"
    val LevelSelectionSymbol = "⡿"
    val GameURL = "https://benkeks.itch.io/clave"
    val JustPlayed = "Just played Clave"
  }

  private val hudContainer = dom.document.createElement("div")
  hudContainer.id = "hud"

  private val scoreTextNode = dom.document.createElement("p")
  scoreTextNode.id = "level-info"
  scoreTextNode.classList.add("score")
  hudContainer.appendChild(scoreTextNode)
  private val scoreText = dom.document.createTextNode("")
  scoreTextNode.appendChild(scoreText)

  private val pauseButton = dom.document.createElement("button").asInstanceOf[dom.raw.HTMLElement]
  private val pauseButtonText = dom.document.createTextNode(Texts.PauseSymbol)
  pauseButton.classList.add("btn")
  pauseButton.classList.add("btn-secondary")
  pauseButton.appendChild(pauseButtonText)
  pauseButton.addEventListener("click", clickPause _)
  hudContainer.appendChild(pauseButton)

  private val switchButton = dom.document.createElement("button").asInstanceOf[dom.raw.HTMLElement]
  switchButton.classList.add("btn")
  switchButton.classList.add("btn-secondary")
  switchButton.classList.add("d-none")
  switchButton.appendChild(dom.document.createTextNode(Texts.LevelSelectionSymbol))
  switchButton.addEventListener("click", clickSwitch _)
  hudContainer.appendChild(switchButton)

  private val levelList = dom.document.createElement("div")
  levelList.id = "level-list"
  hudContainer.appendChild(levelList)
  private val levelButtons = scala.collection.mutable.Map[String, dom.raw.HTMLElement]()

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

    val tmpDrawingCanvas = dom.document.createElement("canvas").asInstanceOf[dom.raw.HTMLCanvasElement]
    val renderingContext = tmpDrawingCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    val levelPreviewer = new LevelPreviewer(renderingContext)
    game.levelDownloader.levelList.foreach { levelId =>
      val level = game.levelDownloader.getLevelById(levelId).get
      val levelButton = dom.document.createElement("button").asInstanceOf[dom.raw.HTMLElement]
      levelButton.classList.add("btn")
      levelButton.classList.add("btn-secondary")
      levelButton.classList.add("level-sel")
      val icon = dom.document.createElement("img").asInstanceOf[dom.raw.HTMLImageElement]
      icon.src = levelPreviewer.getBase64(level)
      icon.width = level.width * 3
      icon.height = level.height * 3
      levelButton.appendChild(icon)
      levelButton.appendChild(dom.document.createTextNode(level.name))
      val span = dom.document.createElement("span").asInstanceOf[dom.raw.HTMLSpanElement]
      span.classList.add("score")
      span.appendChild(dom.document.createTextNode("(0)"))
      levelButton.appendChild(span)
      levelButton.addEventListener("click", selectLevel(levelId) _)
      levelList.appendChild(levelButton)
      levelButtons(levelId) = levelButton
    }
    val feedbackButton = dom.document.createElement("a").asInstanceOf[dom.raw.HTMLElement]
    val plainUrl = Texts.GameURL
    val encodedUrl = URIUtils.encodeURI(plainUrl)
    val encodedTweet = URIUtils.encodeURI(Texts.JustPlayed)
    feedbackButton.classList.add("btn")
    feedbackButton.classList.add("btn-secondary")
    feedbackButton.classList.add("level-sel")
    feedbackButton.classList.add("social-button")
    feedbackButton.setAttribute("href", plainUrl)
    feedbackButton.setAttribute("target", "_blank")
    feedbackButton.innerHTML = s"""
      ❤ / 🐛<br>If you like Clave or discover bugs, tell @benkeks on <a href="$plainUrl" target="_blank">itch.io</a> or on <a href="https://twitter.com/intent/tweet?text=$encodedTweet&hashtags=clave&url=$encodedUrl&via=benkeks" target="_blank">Twitter</a>!
    """
    levelList.appendChild(feedbackButton)
  }

  def setLevelHighScore(score: Int): Unit = {
    scoreText.textContent = "Level high score: "+score
  }

  /**
    * @param text popup text to be displayed
    * @param time if set, remove the the popup after `time` milliseconds
    */
  def setPopup(text: String, time: Double = 0, delay: Double = 0): Unit = {
    popupText = text
    if (text == "") {
      popup.classList.remove("visible")
    } else {
      if (delay > 0) {
        schedule(lastFrameTime + delay) { () =>
          popup.innerHTML = popupText
          popup.classList.add("visible")
        }
      } else {
        popup.innerHTML = popupText
        popup.classList.add("visible")
      }
    }
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

  def updateLevelListDisplay(game: Game) = {
    levelButtons.foreach { case (id, btn) =>
      if (game.levelScores.isDefinedAt(id)) {
        btn.classList.remove("d-none")
        btn.children(1).innerText = s"(${game.levelScores(id)})"
        if (game.scoreHasBeenUpdated(id))
          btn.classList.add("score-updated")
        if (game.currentLevelId == id)
          btn.classList.add("current-level")
        else
          btn.classList.remove("current-level")
      } else {
        btn.classList.add("d-none")
      }
    }
  }

  def notifyGameState(): Unit = {
    pauseButtonText.textContent = Texts.PauseSymbol
    Game.GameStateIds.foreach(hudContainer.classList.remove(_))
    for (
      g <- game
    ) {
      hudContainer.classList.add(Game.gameStateToId(g.state))
      g.state match {
        case Game.LevelScreen() | Game.StartUp(_) =>
          pauseButtonText.textContent = Texts.ContinueSymbol
          updateLevelListDisplay(game.get)
          showHide(switchButton, show = false)
          showHide(pauseButton, show = true)
        case Game.Paused() =>
          pauseButtonText.textContent = Texts.ContinueSymbol
          showHide(switchButton, show = true)
          showHide(pauseButton, show = true)
        case Game.Continuing() =>
          overlay.classList.remove("scene-fadein")
          overlay.classList.add("scene-fadeout")
          showHide(switchButton, show = false)
          showHide(pauseButton, show = true)
        case Game.Lost() | Game.Won(_, _, _) =>
          overlay.classList.remove("scene-fadeout")
          overlay.classList.add("scene-fadein")
          showHide(switchButton, show = false)
          showHide(pauseButton, show = false)
        case _ =>
          overlay.classList.remove("scene-fadeout")
          overlay.classList.add("scene-fadein")
          showHide(switchButton, show = false)
          showHide(pauseButton, show = true)
      }
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