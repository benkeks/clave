package net.mrkeks.clave.game

import net.mrkeks.clave.util.TimeManagement
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.view.Input
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.map.MapData
import net.mrkeks.clave.view.GUI
import net.mrkeks.clave.map.LevelDownloader
import net.mrkeks.clave.game.abstracts.GameObjectManagement
import net.mrkeks.clave.game.objects._
import net.mrkeks.clave.game.characters._
import net.mrkeks.clave.view.PlayerControl

import org.denigma.threejs.Vector3
import scala.scalajs.js
import org.scalajs.dom
import net.mrkeks.clave.util.Mathf
import net.mrkeks.clave.util.markovIf

class Game(val context: DrawingContext, val input: Input, val gui: GUI, val levelDownloader: LevelDownloader)
    extends GameObjectManagement with GameLevelLoader with ProgressTracking with TimeManagement
    with Input.ActionKeyListener with Input.MenuKeyListener {

  import Game._

  var state: State = StartUp(0)
  var lastStateChangeTime: Double = 0.0

  var map: GameMap = null
  
  var player: Option[Player] = None
  
  var playerControl: PlayerControl = null

  private var settingsDifficulty: Difficulty = loadDifficulty()
  private var levelDifficulty: Difficulty = settingsDifficulty

  loadProgress()

  gui.registerGame(this)

  context.particleSystem.registerParticleType("gfx/dust.png", "dust")
    .setGravity(.0000004)
    .setDecay(.0015)
    .setGrowth(.005)
  context.particleSystem.registerParticleType("gfx/dust.png", "point", additive = true)
    .setGravity(-.025 / 1000)
    .setDecay(1.0 / 1300)
    .setGrowth(-1.0 / 2000)
  context.particleSystem.registerParticleType("gfx/shadow.gif", "spark", maxAmount = 100, additive = true)
    .setDecay(.001)
    .setGrowth(.0015)
  private val bgParticles = context.particleSystem.registerParticleType("gfx/dust.png", "bgfog")
    .setGravity(.00000012)
    .setDecay(.00005)
    .setGrowth(.0008)
  private var bgParticleTimer = 0.0

  input.actionKeyListeners.addOne(this)
  input.menuKeyListeners.addOne(this)

  def getPlayerPositions = {
    player.flatMap(_.getPositionOnMap).toList
  }
  
  def update(timeStamp: Double): Unit = {

    input.update(timeStamp)

    handleState()

    context.render(deltaTime)

    updateTime(timeStamp)
  }

  def handleState(): Unit = {

    context.particleSystem.update(deltaTime)

    updateBackground()

    state match {
      case s @ StartUp(anim) =>
        tickedTimeLoop {
          gameObjects.foreach(_.update(tickTime))

          updateGameObjectList()
        }
        s.anim = Math.min(1.0, anim + .0004 * deltaTime)
        val animProgress = 160.0 - 150.0 * Mathf.quadTo(.8, s.anim)
        context.cameraUpdatePosition(new Vector3(map.center.x, map.center.y, map.center.z * 3), spectatorOffSet = animProgress)
      case LevelScreen() =>
      case Narration(_) =>
        tickedTimeLoop {
          gameObjects.foreach(_.update(tickTime * .2))

          updateGameObjectList()
        }
        player.foreach { p =>
          context.cameraLookAt(p.getPosition)
        }
        if (lastStateChangeTime + 3500 < lastFrameTime) {
          gui.setHint(input.renderInputHint("$DoAction to continue."))
        }
      case Running() =>
        playerControl.update(deltaTime)

        tickedTimeLoop {
          gameObjects.foreach(_.update(tickTime))

          updateGameObjectList()
        }

        if (lastStateChangeTime + 3500 < lastFrameTime && playerControl != null && (!playerControl.hasEverMoved || !playerControl.hasEverActed)) {
          if (!playerControl.hasEverMoved) {
            gui.setHint(input.renderInputHint("$DoNavigate to move around."))
          } else if (!playerControl.hasEverActed) {
            gui.setHint(input.renderInputHint("$DoAction to pick up a box you touch or to place a box you carry."))
          }
        } else {
          gui.setHint("")
        }

        if (player.isDefined &&
            player.get.state.isInstanceOf[PlayerData.Dead]) {
          setState(Lost("The monsters crushed you!"))
        } else if (player.isDefined &&
            player.get.state.isInstanceOf[PlayerData.Poisoned]) {
          setState(Lost("The monsters poisoned you!"))
        } else if (player.isDefined && player.get.state.isInstanceOf[PlayerData.Frozen]) {
          setState(Lost("You've been deep-frozen."))
        } else if (playerIsSurrounded()) {
          setState(Lost("The monsters got you surrounded!"))
        } else {
          val friends = gameObjects.collect { case m: Monster if m.kind == MonsterData.FriendlyMonster => m }
          if (friends.exists(_.state.isInstanceOf[MonsterData.Frozen])) {
            setState(Lost("A friend has been frozen!"))
          } else if (friends.isEmpty) {
            checkVictory()
          }
        }
      case Paused() =>
        if (lastStateChangeTime + 3000 < lastFrameTime) {
          gui.setHint(s"Press ${gui.Texts.ContinueSymbol} button to continue.")
        }
      case s @ Won(score, victoryRegion, victoryDrawProgress) =>
        tickedTimeLoop {
          gameObjects.foreach(_.update(tickTime))
          updateGameObjectList()
        }
        if ((victoryDrawProgress * 4).toInt % 2 == 0 && victoryRegion.nonEmpty) {
          context.audio.play("victory-drawing")
        }
        s.victoryDrawProgress += deltaTime * .02
        val pointTar = player.get.getPosition
        s.victoryRegion = victoryRegion.dropWhile { case (x, z) =>
          val flyTime = 1000 + 30 * pointTar.distanceTo(new Vector3(x,0,z))
          context.particleSystem.emitParticle("point", x, -.4, z, (pointTar.x - x) / flyTime, .025 * flyTime / 2000, (pointTar.z - z) / flyTime, .93, .93, .47, 1.0, .8 + .5 * Math.random())
          map.victoryLighting(x, z) < s.victoryDrawProgress
        }
        if (lastStateChangeTime + 2500 < lastFrameTime) {
          gui.setHint(input.renderInputHint("$DoAction to continue."))
        }
      case Lost(_) =>
        tickedTimeLoop {
          gameObjects.foreach(_.update(tickTime))
          updateGameObjectList()
        }
        if (lastStateChangeTime + 2500 < lastFrameTime) {
          gui.setHint(input.renderInputHint("$DoAction to continue."))
        }
      case Continuing() =>
        player.foreach { p => 
          p.update(deltaTime)
          if (p.getPosition.y > 70) {
            switchLevelById(upcomingLevelId.get)
            setState(Running())
          }
        }
    }

    if (!state.isInstanceOf[StartUp] && !state.isInstanceOf[Narration]) {
      player.foreach { p =>
        context.cameraUpdatePosition(p.getPosition)
      }
    }
    val newZoom = Mathf.approach(context.camera.zoom,
      (if (state.isInstanceOf[Paused] || state.isInstanceOf[Won]) .7 else if (state.isInstanceOf[Lost]) 1.3 else 1.0),
      (if (state.isInstanceOf[Lost] || state.isInstanceOf[Won]) .0001 else 0.01) * deltaTime)
    if (newZoom != context.camera.zoom) {
      context.camera.zoom = newZoom
      context.camera.updateProjectionMatrix()
    }
  }
  
  def setState(newState: State): Unit = {
    state match {
      case Narration(_) =>
        timeSpeed = 1.0
        gui.setNarration("")
      case _ =>
    }
    gui.setHint("")

    newState match {
      case StartUp(_) =>
        context.audio.playAtmosphere("music-boxin-monsters", 1.0, .001, Some(context.audio.musicListener))
      case LevelScreen() =>
      case Narration(message) =>
        gui.setNarration(message)
        timeSpeed = .2
      case Running() =>
        context.audio.playAtmosphere("music-boxin-monsters", 1.0, .001, Some(context.audio.musicListener))
        state match {
          case Paused() =>
            context.audio.play("game-unpaused")
          case Narration(_) =>
            context.audio.play("button-click")
          case _ =>
            context.audio.play("level-start")
        }
      case Paused() =>
        context.audio.play("game-paused")
      case Continuing() =>
      case Won(levelScore, _, _) =>
        val previousScore = getScoreForDifficulty(currentLevelId, levelDifficulty)
        context.audio.setAtmosphereVolume("music-boxin-monsters", .3)
        bookScore(currentLevelId, levelScore, levelDifficulty)
        val msgPart1 = if (previousScore == 0) {
          "Yeah, a new success!"
        } else if (previousScore < levelScore) {
          "Wow, new high score!"
        } else if (previousScore < levelScore * 1.5) {
          markovIf(List("Great!", "Marvelous!", "Woha!", "Impressive!"))
        } else {
          markovIf(List("Yay!", "Nice!", "Ooookay!"))
        }
        gui.setPopup(s"""
          <div class='message'>
            <p><strong>You cleared <span class="score">$levelScore</span> fields.</strong></p>
            <p>${currentLevel.map(_.renderScoreForDifficulty(levelScore, levelDifficulty)).getOrElse("")}</p>
            <p>$msgPart1</p>
          </div>""", delay = 500 + levelScore * 2)
        schedule(lastFrameTime + 500 + levelScore * 2) { () =>
          context.audio.play("level-won")
        }
        playerControl.resetState()
      case Lost(reason) => 
        context.audio.play("level-lost")
        context.audio.setAtmosphereVolume("music-boxin-monsters", .2)
        gui.setPopup(s"""
          <div class='message'>
            <p>Oh no!</p>
            <p><strong>${reason}</strong></p>
          </div>""", delay = 500)
        playerControl.resetState()
    }
    state = newState
    lastStateChangeTime = lastFrameTime
    gui.notifyGameState()
  }

  def handleActionKey(): Unit = {
    state match {
      case Narration(message) =>
        if (lastFrameTime - lastStateChangeTime > 1500) {
          setState(Running())
        }
      case Won(_, _, _) | Lost(_) =>
        if (lastFrameTime - lastStateChangeTime > 1500) {
          continueLevel()
        }
      case _ =>
    }
  }

  def handleMenuKey(): Unit = {
    togglePause()
  }

  private def updateBackground(): Unit = {
    val rotationDir = if (state.isInstanceOf[Won]) -1 else if (state.isInstanceOf[Paused]) .2 else 1
    bgParticles.mesh.rotation.y += .00003 * rotationDir * deltaTime
    if (map != null) bgParticles.mesh.position.copy(map.center) else bgParticles.mesh.position.set(8,0,8)
    if (state.isInstanceOf[Paused] || state.isInstanceOf[LevelScreen]) {
      bgParticles.mesh.scale.y = Mathf.approach(bgParticles.mesh.scale.y, 0.5, .003 * deltaTime)
    } else if (state.isInstanceOf[Lost]) {
      bgParticles.mesh.scale.y = Mathf.approach(bgParticles.mesh.scale.y, 0.4, .00002 * deltaTime)
    } else {
      bgParticles.mesh.scale.y = Mathf.approach(bgParticles.mesh.scale.y, 1.0, .003 * deltaTime)
    }
    bgParticles.mesh.scale.x = bgParticles.mesh.scale.y
    bgParticles.mesh.scale.z = bgParticles.mesh.scale.y
    bgParticles.mesh.position.z += (1.0 - bgParticles.mesh.scale.y) * 10.0
    bgParticles.mesh.rotation.x = (bgParticles.mesh.scale.y - 1.0) * Math.PI * (if (state.isInstanceOf[Lost]) -1.0 else 1.0)
    if (lastFrameTime > bgParticleTimer) {
      val baseBrightness = if (state.isInstanceOf[Lost]) -.1 else .5
      val deg = 2 * Math.PI * Math.random()
      val dist = (if (map != null) map.center.asInstanceOf[js.Dynamic].manhattanLength().asInstanceOf[Double] else 14) + 4 * Math.random()
      context.particleSystem.emitParticle("bgfog",
        Math.cos(deg) * dist, -8 + 8 * Math.random(), Math.sin(deg) * dist,
        -.0005 + .001 * Math.random(), -.0005 + .001 * Math.random(), -.0005 + .001 * Math.random(),
        baseBrightness + .5 * Math.random(), baseBrightness + .5 * Math.random(), baseBrightness + .5 * Math.random(), .5 + .3 * Math.random(),
        -.5 + 1.5 * Math.random())
      bgParticleTimer = lastFrameTime + 150
    }
  }

  private def checkVictory(): Unit = {
    if (state.isInstanceOf[Running]) {
      val victoryRegion = map.checkVictory(getPlayerPositions)
      val levelScore = victoryRegion.length
      if (levelScore > 0) {
        setState(Won(levelScore, victoryRegion, 0))
      }
    }
  }

  private def playerIsSurrounded(): Boolean = {
    player.flatMap(_.getPositionOnMap).exists { playerPosition =>
      map.getAdjacentPositions(playerPosition).forall { case xz @ (x,z) =>
        map.isTilePermanentlyBlocked(x,z) ||
        player.exists(_.state.isInstanceOf[PlayerData.Carrying]) && map.intersectsLevel(x,z, considerObstacles = false) ||
        map.getObjectsAt(xz).exists { case m: Monster => m.kind != MonsterData.FriendlyMonster; case _ => false }
      }
    }
  }
  
  def continueLevel(): Unit = {
    if (state.isInstanceOf[Won] || state.isInstanceOf[Lost]) {
      gui.setPopup("")
      currentLevelNum += (if (state.isInstanceOf[Won]) 1 else 0)
      val nextLevelId = levelDownloader.getLevelIdByNum(currentLevelNum)
      unlockLevel(nextLevelId)
      player.foreach(_.setState(PlayerData.Spawning(ySpeed = 0.05)))
      setState(Continuing())
    }
  }

  def switchLevelById(id: String): Unit = {
    unloadLevel()
    levelDifficulty = settingsDifficulty
    loadLevelById(id, levelDifficulty)
    for (l <- currentLevel) {
      gui.setPopup(s"<div class='level-name'>${l.name}</div>", time = 2000)
      gui.setLevelHighScore(l, getScoreForDifficulty(id, levelDifficulty))
    }
    if (playerControl == null) {
      playerControl = new PlayerControl(player.get, input)
    } else {
      playerControl.reassign(player.get)
    } 
    player.get.setState(PlayerData.Spawning(ySpeed = -.06))
    gameObjects.foreach {
      case m: Meta => m.registerGame(this)
      case _ =>
    } 
  }

  def togglePause() = {
    state match {
      case Paused() => setState(Running())
      case Narration(_) | Running() => setState(Paused())
      case StartUp(_) =>
        switchLevelById(upcomingLevelId.get)
        setState(Running())
      case LevelScreen() =>
        if (map == null) {
          switchLevelById(upcomingLevelId.get)
        }
        setState(Running())
      case _ =>
    }
  }

  def loadDifficulty(): Difficulty = {
    val txt = dom.window.localStorage.getItem(DifficultyKey)
    if (txt == null) {
      Difficulty.Easy
    } else {
      Difficulty.apply(txt.toIntOption.getOrElse(0))
    }
  }

  def setDifficulty(difficulty: Difficulty) = {
    dom.window.localStorage.setItem(DifficultyKey, difficulty.id.toString())
    this.settingsDifficulty = difficulty
  }

  def getDifficultySetting() = settingsDifficulty
  def getLevelDifficulty() = levelDifficulty

  def showMeta(message: String) = {
    setState(Narration(message))
  }
}

object Game {
  abstract sealed class State
  case class StartUp(var anim: Double) extends State
  case class LevelScreen() extends State
  case class Narration(message: String) extends State
  case class Running() extends State
  case class Paused() extends State
  case class Won(levelScore: Int, var victoryRegion: List[(Int, Int)], var victoryDrawProgress: Double) extends State
  case class Lost(reason: String) extends State
  case class Continuing() extends State

  def gameStateToId(s: State) = s match {
    case StartUp(_) => "startup"
    case LevelScreen() => "levelscreen"
    case Narration(_) => "narration"
    case Running() => "running"
    case Paused() => "paused"
    case Won(_, _, _) => "won"
    case Lost(_) => "lost"
    case Continuing() => "continuing"
  }

  val GameStateIds = List("startup", "levelscreen", "narration", "running", "paused", "won", "lost", "continuing")

  object Difficulty extends Enumeration {
    val Easy, Hard = Value
  }
  type Difficulty = Difficulty.Value

  val DifficultyKey = net.mrkeks.clave.game.ProgressTracking.ClavePrefix + "difficulty"
}