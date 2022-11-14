package net.mrkeks.clave.game

import net.mrkeks.clave.util.TimeManagement
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.view.Input
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.map.MapData
import net.mrkeks.clave.view.GUI
import net.mrkeks.clave.map.LevelDownloader
import net.mrkeks.clave.game.abstracts.GameObjectManagement
import net.mrkeks.clave.game.objects.Crate
import net.mrkeks.clave.game.objects.Gate
import net.mrkeks.clave.game.objects.Trigger
import net.mrkeks.clave.game.objects.TriggerGroup
import net.mrkeks.clave.view.PlayerControl
import net.mrkeks.clave.game.characters.Player
import net.mrkeks.clave.game.characters.PlayerData
import net.mrkeks.clave.game.characters.Monster

import org.denigma.threejs.Vector3
import scala.scalajs.js
import net.mrkeks.clave.util.Mathf
import net.mrkeks.clave.util.markovIf

class Game(val context: DrawingContext, val input: Input, val gui: GUI, val levelDownloader: LevelDownloader)
  extends GameObjectManagement with GameLevelLoader with ProgressTracking with TimeManagement {

  import Game._

  var state: State = StartUp(0)

  var map: GameMap = null
  
  var player: Option[Player] = None
  
  var playerControl: PlayerControl = null

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

  def getPlayerPositions = {
    player.flatMap(_.getPositionOnMap).toList
  }
  
  def update(timeStamp: Double): Unit = {

    input.update(timeStamp)

    handleState()

    context.render()

    updateTime(timeStamp)
  }

  def handleState(): Unit = {

    context.particleSystem.update(deltaTime)

    updateBackground()

    state match {
      case s @ StartUp(anim) =>
        tickedTimeLoop {
          gameObjects.foreach(_.update(tickTime))

          removeAllMarkedForDeletion()
        }
        s.anim = Math.min(150, anim + .04 * deltaTime)
        context.cameraUpdatePosition(new Vector3(map.center.x, map.center.y, map.center.z * 3), spectatorOffSet = 160.0 - anim)
      case LevelScreen() =>
      case Running() =>
        playerControl.update(deltaTime)

        tickedTimeLoop {
          gameObjects.foreach(_.update(tickTime))

          removeAllMarkedForDeletion()
        }
        
        if (player.isDefined &&
            (player.get.state.isInstanceOf[PlayerData.Dead])
            || player.get.state.isInstanceOf[PlayerData.Frozen]) {
          setState(Lost())
        } else if (playerIsSurrounded()) {
          setState(Lost())
        } else {
          checkVictory()
        }
      case Paused() =>
        //
      case s @ Won(score, victoryRegion, victoryDrawProgress) =>
        tickedTimeLoop {
          gameObjects.foreach(_.update(tickTime))
          removeAllMarkedForDeletion()
        }
        s.victoryDrawProgress += deltaTime * .02
        val pointTar = player.get.getPosition
        s.victoryRegion = victoryRegion.dropWhile { case (x, z) =>
          val flyTime = 1000 + 30 * pointTar.distanceTo(new Vector3(x,0,z))
          context.particleSystem.emitParticle("point", x, -.4, z, (pointTar.x - x) / flyTime, .025 * flyTime / 2000, (pointTar.z - z) / flyTime, .93, .93, .47, 1.0, .8 + .5 * Math.random())
          map.victoryLighting(x, z) < s.victoryDrawProgress
        }
      case Lost() =>
        tickedTimeLoop {
          gameObjects.foreach(_.update(tickTime))
          removeAllMarkedForDeletion()
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

    if (!state.isInstanceOf[StartUp]) {
      player.foreach { p =>
        context.cameraUpdatePosition(p.getPosition)
      }
    }
  }
  
  def setState(newState: State): Unit = {
    newState match {
      case StartUp(_) =>
      case LevelScreen() =>
      case Running() =>
      case Paused() =>
      case Continuing() =>
      case Won(levelScore, _, _) =>
        val previousScore = levelScores.get(currentLevelId).getOrElse(0)
        bookScore(currentLevelId, levelScore)
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
            <p>$msgPart1</p>
            <p><strong>You scored <span class="score">$levelScore</span> points.</strong></p>
          </div>""", delay = 500 + levelScore * 2)
        input.keyPressListener.addOne(" ", (this, continueLevel _))
        playerControl.resetState()
      case Lost() => 
        gui.setPopup(s"""
          <div class='message'>
            <p>Oh no!</p>
            <p><strong>The monsters got you!</strong></p>
          </div>""", delay = 500)
        input.keyPressListener.addOne(" ", (this, continueLevel _))
    }
    state = newState
    gui.notifyGameState()
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
        player.exists(_.state.isInstanceOf[PlayerData.Carrying]) && map.intersectsLevel(x,z, considerObstacles = true) ||
        map.getObjectsAt(xz).exists(_.isInstanceOf[Monster])
      }
    }
  }
  
  def continueLevel(): Unit = {
    if (state.isInstanceOf[Won] || state.isInstanceOf[Lost]) {
      input.keyPressListener.filterNot {case (key, (tar, _)) => tar == this && key == " " }
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
    loadLevelById(id)
    for (l <- currentLevel) {
      gui.setPopup(s"<div class='level-name'>${l.name}</div>", time = 2000)
    }
    gui.setLevelHighScore(levelScores(id))
    if (playerControl != null) playerControl.clear()
    playerControl = new PlayerControl(player.get, input)
    player.get.setState(PlayerData.Spawning(ySpeed = -.06))
  }

  def togglePause() = {
    state match {
      case Paused() => setState(Running())
      case Running() => setState(Paused())
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
}

object Game {
  abstract sealed class State
  case class StartUp(var anim: Double) extends State
  case class LevelScreen() extends State
  case class Running() extends State
  case class Paused() extends State
  case class Won(levelScore: Int, var victoryRegion: List[(Int, Int)], var victoryDrawProgress: Double) extends State
  case class Lost() extends State
  case class Continuing() extends State

  def gameStateToId(s: State) = s match {
    case StartUp(_) => "startup"
    case LevelScreen() => "levelscreen"
    case Running() => "running"
    case Paused() => "paused"
    case Won(_, _, _) => "won"
    case Lost() => "lost"
    case Continuing() => "continuing"
  }

  val GameStateIds = List("startup", "levelscreen", "running", "paused", "won", "lost", "continuing")
}