package net.mrkeks.clave.game

import scala.scalajs.js
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.view.Input
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.map.MapData
import net.mrkeks.clave.view.GUI
import net.mrkeks.clave.map.Level

class Game(context: DrawingContext, input: Input, gui: GUI) {
  
  abstract sealed class State
  case class StartUp() extends State
  case class Running() extends State
  case class Paused() extends State
  case class Won(levelScore: Int) extends State
  case class Lost() extends State
  
  var state: State = StartUp()
  
  var score = 0
  var levelId = 0
  
  /** List of individual objects in the game (movable stuff, enemies, the player..)  */
  var gameObjects = List[GameObject]()
  var gameObjectIdCount = 0
  
  var lastFrameTime = js.Date.now
  
  /** Time that passed since the last frame. (in ms) */
  var deltaTime = 0.0
  
  private var map: GameMap = null
  
  private var player: Player = null
  
  private var playerControl: PlayerControl = null
  
  def getPlayerPositions = {
    player.getPositionOnMap.toList
  }
  
  def add(o: GameObject) {
    gameObjects = o :: gameObjects
    gameObjectIdCount += 1
    o.id = gameObjectIdCount
    o.init(context)
  }
  
  def remove(o: GameObject) {
    gameObjects = gameObjects.filterNot(_.id == o.id)
    o.clear(context)
  }
  
  def clear() {
    playerControl.clear()
    gameObjects.foreach(remove)
  }
  
  def update() {
    state match {
      case StartUp() =>
      case Running() =>
        playerControl.update(deltaTime)
        gameObjects.foreach(_.update(deltaTime))
        if (player.state.isInstanceOf[PlayerData.Dead]) {
          setState(Lost())
        }
      case Paused() =>
        //
      case Won(score) =>
        
      case Lost() =>
        
    }
    
    context.render()
    
    deltaTime = js.Date.now - lastFrameTime
    lastFrameTime = js.Date.now
  }
  
  def unloadLevel() {
    clear()
  }
  
  def loadLevel(id: Int) {
    levelId = id
    loadLevel(Level.levels(levelId))
  }
  
  def loadLevel(level: Level) {
    map = new GameMap(this, level.width, level.height)
    
    val positions = map.loadFromString(level.mapCsv)
    map.updateView()
    add(map)
    
    player = new Player(map)
    add(player)
    playerControl = new PlayerControl(player, input)
    
    for {
      playerPositions <- positions.get(MapData.Tile.Player)
      (x, z) <- playerPositions.headOption
    } {
      player.setPosition(x, 0, z)
    }
        
    val cratePositions = positions.getOrElse(MapData.Tile.Wall, List())
    cratePositions.foreach { case (x,z) =>
      val crate = new Crate(map)
  
      add(crate)
      crate.place(x, z)
    }
    
    val monsterPositions = positions.getOrElse(MapData.Tile.Monster, List())
    monsterPositions.foreach { case (x,z) =>
      val monster = new Monster(map)
      add(monster)
      monster.setPosition(x, 0, z)
    }
  }
  
  def setState(newState: State) {
    newState match {
      case StartUp() =>
      case Running() =>
      case Paused() =>
      case Won(levelScore) =>
        score += levelScore
        gui.setScore(score)
        input.keyPressListener.addBinding(32, continueLevel)
      case Lost() => 
        score -= 50
        gui.setScore(score)
        input.keyPressListener.addBinding(32, continueLevel)
    }
    state = newState
  }
  
  def notifyVictory(levelScore: Int) {
    if (state.isInstanceOf[Running]) {
      println("victory! "+levelScore)
      setState(Won(levelScore))
    }
  }
  
  def continueLevel(): Unit = {
    if (state.isInstanceOf[Won] || state.isInstanceOf[Lost]) {
      input.keyPressListener.removeBinding(32, continueLevel)
      unloadLevel()
      loadLevel(levelId + (if (state.isInstanceOf[Won]) 1 else 0))
      setState(Running())
    }
  }
}