package net.mrkeks.clave.view

import net.mrkeks.clave.game.Game
import net.mrkeks.clave.map.LevelPreviewer
import net.mrkeks.clave.map.Level
import net.mrkeks.clave.util.TimeManagement
import net.mrkeks.clave.game.ProgressTracking
import net.mrkeks.clave.Clave

import scala.collection.mutable.Map

import org.scalajs.dom
import scala.scalajs.js.URIUtils

class GUI() extends TimeManagement with Input.ArrowKeyListener with Input.ActionKeyListener {

  object Texts {
    val ContinueSymbol = "▶"
    val ContinueDescription = "Start/continue current level"
    val PauseSymbol = "☰"
    val PauseDescription = "Pause game and show menu"
    val LevelSelectionSymbol = "⡿"
    val RestartSymbol = "↻"
    val RestartDescription = "Retry the current level"
    val LevelSelectionDescription = "Show level selection"
    val VolumeSymbol = "🔊"
    val VolumeDescription = "Game sound volume"
    val MusicSymbol = "🎵"
    val MusicDescription = "Game music volume"
    val GfxDetailSymbol = "📺 HD graphics"
    val GfxDetailDescription = "Deactivate for better performance! (Requires reload)"
    val GfxFullscreenSymbol = "⛶ Full screen"
    val GfxFullscreenDescription = "Toggle full screen mode"
    val HardModeSymbol = "☠ Hard mode"
    val HardModeDescription = "Play levels with bigger monsters"
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

  private val pauseButton = dom.document.createElement("button").asInstanceOf[dom.HTMLElement]
  private val pauseButtonText = dom.document.createTextNode(Texts.PauseSymbol)
  pauseButton.title = Texts.PauseDescription
  pauseButton.classList.add("btn")
  pauseButton.classList.add("btn-secondary")
  pauseButton.classList.add("btn-lg")
  pauseButton.appendChild(pauseButtonText)
  pauseButton.addEventListener("click", clickPause _)
  pauseButton.addEventListener("mouseenter", playHoverSound)
  hudContainer.appendChild(pauseButton)

  private val switchButton = dom.document.createElement("button").asInstanceOf[dom.HTMLElement]
  switchButton.title = Texts.LevelSelectionDescription
  switchButton.classList.add("btn")
  switchButton.classList.add("btn-secondary")
  switchButton.classList.add("btn-lg")
  switchButton.classList.add("d-none")
  switchButton.appendChild(dom.document.createTextNode(Texts.LevelSelectionSymbol))
  switchButton.addEventListener("click", clickSwitch _)
  switchButton.addEventListener("mouseenter", playHoverSound)
  hudContainer.appendChild(switchButton)

  private val levelList = dom.document.createElement("div")
  levelList.id = "level-list"
  hudContainer.appendChild(levelList)
  private val levelButtons = scala.collection.mutable.Map[String, dom.HTMLElement]()

  private val overlay = dom.document.createElement("div")
  overlay.id = "overlay"
  hudContainer.appendChild(overlay)

  private val popup = dom.document.createElement("div")
  popup.id = "popup"
  hudContainer.appendChild(popup)
  private var popupText = ""

  private val narration = dom.document.createElement("div")
  narration.id = "narration"
  hudContainer.appendChild(narration)
  private var narrationText = ""

  private val hint = dom.document.createElement("div")
  hint.id = "hint"
  hudContainer.appendChild(hint)
  private var hintText = ""

  private val versionInfo = dom.document.createElement("div")
  versionInfo.id = "version-info"
  versionInfo.innerHTML = ProgressTracking.ClaveVersion + (if (Clave.DevMode) " dev buiild" else "")
  hudContainer.appendChild(versionInfo)

  private val options = dom.document.createElement("form")
  options.id = "options"
  options.classList.add("form-control-lg")
  options.innerHTML = s"""
    <div class="form-inline">
      <label title="${Texts.VolumeDescription}" for="options-volume">${Texts.VolumeSymbol}</label>
      <input id="options-volume" type="range" max="10" class="form-range-input" title="${Texts.VolumeDescription}" onkeydown="event.preventDefault()" />
    </div>
    <div id="options-music-form" class="form-inline">
      <label title="${Texts.MusicDescription}" for="options-music">${Texts.MusicSymbol}</label>
      <input id="options-music" type="range" max="10" class="form-range-input" title="${Texts.MusicDescription}" onkeydown="event.preventDefault()" />
    </div>
    <div class="custom-control custom-switch">
      <input id="options-gfx-detail" type="checkbox" class="custom-control-input" title="${Texts.GfxDetailDescription}" />
      <label class="custom-control-label" for="options-gfx-detail" title="${Texts.GfxDetailDescription}">${Texts.GfxDetailSymbol}</label>
    </div>
    <div class="custom-control custom-switch">
      <input id="options-gfx-fullscreen" type="checkbox" class="custom-control-input" title="${Texts.GfxFullscreenDescription}" />
      <label class="custom-control-label" for="options-gfx-fullscreen" title="${Texts.GfxFullscreenDescription}">${Texts.GfxFullscreenSymbol}</label>
    </div>
    <div class="custom-control custom-switch">
      <input id="options-hard-mode" type="checkbox" class="custom-control-input" title="${Texts.HardModeDescription}" />
      <label class="custom-control-label" for="options-hard-mode" title="${Texts.HardModeDescription}">${Texts.HardModeSymbol}</label>
    </div>
    """

  private val optionsVolume = options.querySelector("#options-volume").asInstanceOf[dom.HTMLInputElement]
  optionsVolume.addEventListener("change", changeVolume _)
  private val optionsMusic = options.querySelector("#options-music").asInstanceOf[dom.HTMLInputElement]
  optionsMusic.addEventListener("change", changeMusicVolume _)
  private val optionsGfxDetail = options.querySelector("#options-gfx-detail").asInstanceOf[dom.HTMLInputElement]
  optionsGfxDetail.addEventListener("change", changeGfxDetail _)
  private val optionsGfxFullscreen = options.querySelector("#options-gfx-fullscreen").asInstanceOf[dom.HTMLInputElement]
  optionsGfxFullscreen.addEventListener("change", changeGfxFullscreen _)
  if (!dom.document.fullscreenEnabled) optionsGfxFullscreen.style.display = "none"
  if (dom.document.fullscreenElement != null) optionsGfxFullscreen.checked = true
  dom.document.addEventListener[dom.Event]("fullscreenchange", _ => optionsGfxFullscreen.checked = (dom.document.fullscreenElement != null))
  private val optionsHardMode = options.querySelector("#options-hard-mode").asInstanceOf[dom.HTMLInputElement]
  optionsHardMode.addEventListener("change", changeDifficulty _)

  hudContainer.appendChild(options)

  dom.document.body.firstElementChild.appendChild(hudContainer)

  var game: Option[Game] = None

  private var virtualNavGrid: List[List[dom.HTMLElement]] = List()

  def registerGame(game: Game): Unit = {
    this.game = Some(game)

    optionsVolume.value = (game.context.audio.loadVolumeConfig() * 10).toInt.toString
    if (optionsVolume.value == "0") {
      optionsMusic.value = "0"
    } else {
      optionsMusic.value = (game.context.audio.loadMusicConfig() * 10).toInt.toString
    }
    optionsGfxDetail.checked = game.context.getGfxDetail()
    optionsHardMode.checked = game.getDifficultySetting() == Game.Difficulty.Hard
    if (!game.hardModeAvailable() && !optionsHardMode.checked) {
      // deactivate / hide hard mode option if there have not been enough won levels
      optionsHardMode.parentElement.style.display = "none"
    }

    val tmpDrawingCanvas = dom.document.createElement("canvas").asInstanceOf[dom.HTMLCanvasElement]
    val renderingContext = tmpDrawingCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    val levelPreviewer = new LevelPreviewer(renderingContext)
    game.levelDownloader.levelList.foreach { levelId =>
      val level = game.levelDownloader.getLevelById(levelId).get
      val levelButton = dom.document.createElement("button").asInstanceOf[dom.HTMLElement]
      levelButton.classList.add("btn")
      levelButton.classList.add("btn-secondary")
      levelButton.classList.add("level-sel")
      val icon = dom.document.createElement("img").asInstanceOf[dom.HTMLImageElement]
      icon.src = levelPreviewer.getBase64(level)
      icon.alt = levelId
      icon.width = level.width * 3
      icon.height = level.height * 3
      levelButton.appendChild(icon)
      levelButton.appendChild(dom.document.createTextNode(level.name))
      val span = dom.document.createElement("span").asInstanceOf[dom.HTMLSpanElement]
      span.classList.add("score")
      span.appendChild(dom.document.createTextNode(""))
      levelButton.appendChild(span)
      levelButton.addEventListener("click", selectLevel(levelId) _)
      levelButton.addEventListener("mouseenter", playHoverSound)
      levelList.appendChild(levelButton)
      levelButtons(levelId) = levelButton
    }
    val feedbackButton = dom.document.createElement("a").asInstanceOf[dom.HTMLElement]
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

  def setLevelHighScore(level: Level, score: Int): Unit = {
    if (score > 0) {
      val grading = level.renderScoreForDifficulty(score, game.get.getLevelDifficulty())
      scoreText.textContent = s"Level high score: $score ($grading)"
    } else {
      scoreText.textContent = ""
    }
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

  def setHint(text: String): Unit = {
    hintText = text
    if (text == "") {
      hint.classList.remove("visible")
      schedule(lastFrameTime + 2000) { () =>
        hint.innerHTML = hintText
      }
    } else {
      hint.innerHTML = hintText
      hint.classList.add("visible")
    }
  }

  def setNarration(text: String): Unit = {
    narrationText = text
    if (text == "") {
      narration.classList.remove("visible")
      schedule(lastFrameTime + 2000) { () =>
        narration.innerHTML = narrationText
      }
    } else {
      narration.innerHTML = narrationText
      narration.classList.add("visible")
    }
  }

  def update(timeStamp: Double): Unit = {
    updateTime(timeStamp)
  }

  def clickPause(ev: org.scalajs.dom.Event): Unit = {
    game foreach (_.togglePause())
    pauseButton.blur()
  }

  def clickSwitch(ev: org.scalajs.dom.Event): Unit = {
    playClickSound()
    game foreach (_.setState(Game.LevelScreen()))
    switchButton.blur()
  }

  def changeVolume(ev: org.scalajs.dom.Event): Unit = {
    game foreach { g =>
      val oldVolume = g.context.audio.getEffectVolume()
      g.context.audio.setEffectVolumeConfig(optionsVolume.valueAsNumber * .1)
      g.context.audio.play("player-crate")
      if (oldVolume == 0 && g.context.audio.getEffectVolume() > 0) {
        optionsMusic.value = "5"
        changeMusicVolume(ev)
      } else if (g.context.audio.getEffectVolume() == 0) {
        optionsMusic.value = "0"
        changeMusicVolume(ev)
      }
    }
    
  }

  def changeMusicVolume(ev: org.scalajs.dom.Event): Unit = {
    game foreach { g =>
      g.context.audio.setMusicVolumeConfig(optionsMusic.valueAsNumber * .1)
      g.context.audio.playAtmosphere("music-boxin-monsters", 1.0, .01, Some(g.context.audio.musicListener))
    }
  }

  def changeGfxDetail(ev: org.scalajs.dom.Event): Unit = {
    game foreach { g =>
      g.context.setGfxDetail(optionsGfxDetail.checked)
      playClickSound()
      if (g.state.isInstanceOf[Game.StartUp]) {
        dom.window.location.reload()
      }
    }
  }

  def changeGfxFullscreen(ev: org.scalajs.dom.Event): Unit = {
    game foreach { g =>
      g.context.setFullscreen(optionsGfxFullscreen.checked)
    }
  }

  def changeDifficulty(ev: org.scalajs.dom.Event): Unit = {
    game foreach (_.setDifficulty(
      if (optionsHardMode.checked)
        Game.Difficulty.Hard
      else
        Game.Difficulty.Easy
    ))
    playClickSound()
  }

  def playClickSound(): Unit = {
    game foreach (_.context.audio.play("button-click"))
  }

  def playHoverSound(ev: org.scalajs.dom.Event = null): Unit = {
    game foreach (_.context.audio.play("button-hover"))
  }

  def selectLevel(levelName: String)(ev: org.scalajs.dom.Event): Unit = {
    playClickSound()
    game foreach { g =>
      g.switchLevelById(levelName)
      g.setState(Game.Running())
    }
    ev.target.asInstanceOf[dom.HTMLElement].blur()
  }

  def updateLevelListDisplay(game: Game) = {
    levelButtons.foreach { case (id, btn) =>
      if (game.levelScores.isDefinedAt(id)) {
        val grading = game.levelDownloader.getLevelById(id).map(_.renderScore(game.levelScores(id))).get
        btn.classList.remove("d-none")
        btn.children(1).innerText = grading
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
    pauseButton.title = Texts.PauseDescription
    Game.GameStateIds.foreach(hudContainer.classList.remove(_))
    for (
      g <- game
    ) {
      hudContainer.classList.add(Game.gameStateToId(g.state))
      g.state match {
        case Game.StartUp(_) =>
          pauseButtonText.textContent = Texts.ContinueSymbol
          pauseButton.title = Texts.ContinueDescription
          updateLevelListDisplay(game.get)
          showHide(switchButton, show = false)
          virtualNavGrid = List(
            List(pauseButton),
            List(optionsVolume),
            List(optionsMusic),
            List(optionsGfxDetail),
            List(optionsGfxFullscreen),
            List(optionsHardMode),
            levelList.children.toList.map(_.asInstanceOf[dom.HTMLElement])
          )
        case Game.LevelScreen() =>
          pauseButtonText.textContent = Texts.ContinueSymbol
          pauseButton.title = Texts.ContinueDescription
          updateLevelListDisplay(game.get)
          showHide(switchButton, show = false)
          virtualNavGrid = List(
            List(pauseButton),
            levelList.children.toList.map(_.asInstanceOf[dom.HTMLElement])
          )
        case Game.Paused() =>
          pauseButtonText.textContent = Texts.ContinueSymbol
          pauseButton.title = Texts.ContinueDescription
          showHide(switchButton, show = true)
          virtualNavGrid = List(
            List(pauseButton),
            List(switchButton),
            List(optionsVolume),
            List(optionsMusic),
            List(optionsGfxDetail),
            List(optionsGfxFullscreen),
            List(optionsHardMode)
          )
        case Game.Continuing() =>
          overlay.classList.remove("scene-fadein")
          overlay.classList.add("scene-fadeout")
          showHide(switchButton, show = false)
          virtualNavGrid = List(
            List(pauseButton)
          )
        case Game.Lost(_) | Game.Won(_, _, _) =>
          pauseButtonText.textContent = Texts.RestartSymbol
          pauseButton.title = Texts.RestartDescription
          overlay.classList.remove("scene-fadeout")
          overlay.classList.add("scene-fadein")
          showHide(switchButton, show = false)
          virtualNavGrid = List(
            List(pauseButton)
          )
        case _ =>
          overlay.classList.remove("scene-fadeout")
          overlay.classList.add("scene-fadein")
          showHide(switchButton, show = false)
          virtualNavGrid = List(
            List(pauseButton)
          )
      }
    }
  }

  private def showHide(button: dom.HTMLElement, show: Boolean = true) = {
    if (show) {
      button.classList.remove("d-none")
      button.classList.add("d-block")
    } else {
      button.classList.remove("d-block")
      button.classList.add("d-none")
    }
  }

  override def handleArrowKey(xDir: Int, yDir: Int): Unit = {
    if (!game.exists(_.state.isInstanceOf[Game.Running])) {
      var focusX = 0
      var focusY = 0
      for (y <- 0 until virtualNavGrid.length) {
        for (x <- 0 until virtualNavGrid(y).length) {
          if (dom.document.activeElement == virtualNavGrid(y)(x)) {
            focusX = x
            focusY = y
          }
        }
      }
      val currentFocus = virtualNavGrid(focusY)(focusX)
      if (xDir != 0) {
        if (currentFocus.isInstanceOf[dom.HTMLInputElement] && currentFocus.asInstanceOf[dom.HTMLInputElement].`type` == "range") {
          if (xDir > 0) {
            currentFocus.asInstanceOf[dom.HTMLInputElement].stepUp()
          } else {
            currentFocus.asInstanceOf[dom.HTMLInputElement].stepDown()
          }
        } else {
          focusX += xDir
          if (focusX >= virtualNavGrid(focusY).length) {
            focusX = 0
          }
          if (focusX < 0) {
            focusX = virtualNavGrid(focusY).length - 1
          }
        }
      } else if (yDir != 0) {
        focusY += yDir
        if (focusY >= virtualNavGrid.length) focusY = 0
        if (focusY < 0) focusY = virtualNavGrid.length - 1
        if (yDir < 0) focusX = virtualNavGrid(focusY).length - 1
        if (yDir > 0) focusX = 0
      }
      virtualNavGrid(focusY)(focusX).focus()
    }
  }

  override def handleActionKey(): Unit = {
    game.map(_.state) match {
      case Some(Game.StartUp(_)) | Some(Game.LevelScreen()) | Some(Game.Paused()) =>
        dom.document.activeElement match {
          case btn: dom.HTMLButtonElement => btn.click()
          case input: dom.HTMLInputElement => input.click()
          case _ =>
        }
      case _ =>
    }
  }
}