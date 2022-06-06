package net.mrkeks.clave.game

import net.mrkeks.clave.util.TimeManagement
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.view.Input
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.map.MapData
import net.mrkeks.clave.view.GUI
import net.mrkeks.clave.map.LevelDownloader
import net.mrkeks.clave.game.objects.Crate
import net.mrkeks.clave.game.objects.Gate
import net.mrkeks.clave.game.objects.Trigger
import net.mrkeks.clave.game.objects.TriggerGroup
import net.mrkeks.clave.view.PlayerControl
import net.mrkeks.clave.game.characters.Player
import net.mrkeks.clave.game.characters.PlayerData

import org.denigma.threejs.Vector3

class Game(val context: DrawingContext, val input: Input, val gui: GUI, val levelDownloader: LevelDownloader)
  extends GameObjectManagement with GameLevelLoader with ProgressTracking with TimeManagement {

  import Game._

  var state: State = StartUp()

  var map: GameMap = null
  
  var player: Option[Player] = None
  
  var playerControl: PlayerControl = null

  gui.registerGame(this)

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

    state match {
      case StartUp() =>
      case LevelScreen() =>
      case Running() =>
        playerControl.update(deltaTime)

        tickedTimeLoop {
          gameObjects.foreach(_.update(tickTime))

          removeAllMarkedForDeletion()
        }
        
        if (player.isDefined && player.get.state.isInstanceOf[PlayerData.Dead]) {
          setState(Lost())
        } else {
          checkVictory()
        }
      case Paused() =>
        //
      case s @ Won(score, victoryDrawX, victoryDrawZ) =>
        val (x, z) = getPlayerPositions.headOption.getOrElse((0,0))
        for (i <- (0 to (deltaTime / 4).toInt)) {
          map.victoryLighting(x+s.victoryDrawX, z+s.victoryDrawZ)
          map.victoryLighting(x+s.victoryDrawX, z-s.victoryDrawZ)
          map.victoryLighting(x-s.victoryDrawX, z+s.victoryDrawZ)
          map.victoryLighting(x-s.victoryDrawX, z-s.victoryDrawZ)
          val radius = s.victoryDrawX + s.victoryDrawZ
          if (s.victoryDrawX == radius && s.victoryDrawZ == 0) {
            s.victoryDrawX = 0
            s.victoryDrawZ = radius + 1
          } else {
            s.victoryDrawX += 1
            s.victoryDrawZ -= 1
          }
        }
      case Lost() =>
        gameObjects.foreach(_.update(deltaTime))
      case Continuing() =>
        player.foreach { p => 
          p.update(deltaTime)
          if (p.getPosition.y > 70) {
            switchLevelById(upcomingLevelId.get)
            setState(Running())
          }
        }
    }

    player.foreach { p => 
      val y = p.getPosition.y
      context.cameraUpdatePosition(p.getPosition)
    }
  }
  
  def setState(newState: State): Unit = {
    newState match {
      case StartUp() =>
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
        } else {
          "Yay!"
        }
        gui.setPopup(s"""
          <div class='message'>
            <p>$msgPart1</p>
            <p><strong>You scored $levelScore points.</strong></p>
          </div>
          <div>
            Hit [Space] to continue!
          </div>""")
        input.keyPressListener.addOne(" ", continueLevel _)
      case Lost() => 
        score = Math.max(0, score - 50)
        gui.setPopup(s"""
          <div class='message'>
            <p>Oh no!</p>
            <p><strong>The monsters got you!</strong></p>
          </div>
          <div>
            Hit [Space] to try again!
          </div>""")
        input.keyPressListener.addOne(" ", continueLevel _)
    }
    state = newState
    gui.notifyGameState()
  }
  
  def checkVictory(): Unit = {
    if (state.isInstanceOf[Running]) {
      val levelScore = map.checkVictory(getPlayerPositions)
      if (levelScore >= 0) {
        setState(Won(levelScore, 0, 0))
      }
    }
  }
  
  def continueLevel(): Unit = {
    if (state.isInstanceOf[Won] || state.isInstanceOf[Lost]) {
      input.keyPressListener.subtractOne(" ", continueLevel _)
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
    playerControl = new PlayerControl(player.get, input)
    player.get.setState(PlayerData.Spawning(ySpeed = -.06))
  }

  def togglePause() = {
    state match {
      case Paused() => setState(Running())
      case Running() => setState(Paused())
      case LevelScreen() => {
        if (map == null) {
          switchLevelById(upcomingLevelId.get)
        }
        setState(Running())
      }
      case _ =>
    }
  }
}

object Game {
  abstract sealed class State
  case class StartUp() extends State
  case class LevelScreen() extends State
  case class Running() extends State
  case class Paused() extends State
  case class Won(levelScore: Int, var victoryDrawX: Int, var victoryDrawZ: Int) extends State
  case class Lost() extends State
  case class Continuing() extends State
}