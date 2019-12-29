package net.mrkeks.clave.game

import net.mrkeks.clave.util.TimeManagement
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.view.Input
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.map.MapData
import net.mrkeks.clave.view.GUI
import net.mrkeks.clave.map.Level
import net.mrkeks.clave.game.objects.Crate
import net.mrkeks.clave.game.objects.Gate
import net.mrkeks.clave.game.objects.Trigger
import net.mrkeks.clave.game.objects.TriggerGroup
import net.mrkeks.clave.view.PlayerControl
import net.mrkeks.clave.game.characters.Player
import net.mrkeks.clave.game.characters.PlayerData

class Game(val context: DrawingContext, val input: Input, val gui: GUI)
  extends GameObjectManagement with GameLevelLoader with TimeManagement {

  import Game._

  var state: State = StartUp()
  
  var score = 0
  var levelId = 0
    
  var map: GameMap = null
  
  var player: Player = null
  
  var playerControl: PlayerControl = null

  gui.registerGame(this)

  def getPlayerPositions = {
    player.getPositionOnMap.toList
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
      case Running() =>
        playerControl.update(deltaTime)

        tickedTimeLoop {
          gameObjects.foreach(_.update(tickTime))
        }
        
        if (player.state.isInstanceOf[PlayerData.Dead]) {
          setState(Lost())
        } else {
          checkVictory()
        }
      case Paused() =>
        //
      case s @ Won(score, victoryDrawX, victoryDrawZ) =>
        val (x, z) = player.getPositionOnMap.getOrElse((0,0))
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
        player.update(deltaTime)
        if (player.getPosition.y > 50) {
          unloadLevel()
          loadLevel(levelId)
          setState(Running())
        }
    }
    
    context.camera.position.y = 20 + player.getPosition.y * .5
  }
  
  def setState(newState: State): Unit = {
    newState match {
      case StartUp() =>
      case Running() =>
      case Paused() =>
      case Continuing() =>
      case Won(levelScore, _, _) =>
        score += levelScore
        gui.setScore(score)
        gui.setPopup(s"""
          <div class='message'>
            <p>You have cleared the level!</p>
            <p><strong>You scored $levelScore points.</strong></p>
          </div>
          <div>
            Hit [Space] to continue!
          </div>""")
        input.keyPressListener.addOne(" ", continueLevel _)
      case Lost() => 
        score -= 50
        gui.setScore(score)
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
        println("victory! "+levelScore)
        setState(Won(levelScore, 0, 0))
      }
    }
  }
  
  def continueLevel(): Unit = {
    if (state.isInstanceOf[Won] || state.isInstanceOf[Lost]) {
      input.keyPressListener.subtractOne(" ", continueLevel _)
      gui.setPopup("")
      levelId += (if (state.isInstanceOf[Won]) 1 else 0)
      player.setState(PlayerData.Spawning(ySpeed = 0.07))
      setState(Continuing())
    }
  }
  
  override def loadLevel(id: Int): Unit = {
    super.loadLevel(id)
    playerControl = new PlayerControl(player, input)
    player.setState(PlayerData.Spawning(ySpeed = -.06))
  }

  def togglePause() = {
    state match {
      case Paused() => setState(Running())
      case Running() => setState(Paused())
      case _ =>
    }
  }
}

object Game {
  abstract sealed class State
  case class StartUp() extends State
  case class Running() extends State
  case class Paused() extends State
  case class Won(levelScore: Int, var victoryDrawX: Int, var victoryDrawZ: Int) extends State
  case class Lost() extends State
  case class Continuing() extends State
}