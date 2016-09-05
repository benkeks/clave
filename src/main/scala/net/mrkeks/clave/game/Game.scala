package net.mrkeks.clave.game

import scala.scalajs.js
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.view.Input
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.map.MapData

class Game(context: DrawingContext, input: Input) {
  
  abstract sealed class State
  case class Running() extends State
  case class Paused() extends State
  case class Won() extends State
  
  var state: State = Running()
  
  /** List of individual objects in the game (movable stuff, enemies, the player..)  */
  var gameObjects = List[GameObject]()
  var gameObjectIdCount = 0
  
  var lastFrameTime = js.Date.now
  
  /** Time that passed since the last frame. (in ms) */
  var deltaTime = 0.0
  
  private val map = new GameMap(this, 16,16)
  add(map)
  
  private val player = new Player(map)
  add(player)
  
  private val playerControl = new PlayerControl(player, input)
  
  def getPlayerPositions = {
    List(player.positionOnMap)
  }
  
  def add(o: GameObject) {
    gameObjects = o :: gameObjects
    gameObjectIdCount += 1
    o.id = gameObjectIdCount
    o.init(context)
  }
  
  def remove(o: GameObject) {
    gameObjects = gameObjects.filterNot(_.id == o.id)
    o.clear()
  }
  
  def clear() {
    gameObjects.foreach(_.clear())
  }
  
  def update() {
    state match {
      case Running() =>
        playerControl.update(deltaTime)
        gameObjects.foreach(_.update(deltaTime))
      case Paused() =>
        //
      case Won() =>
        //
    }
    
    context.render()
    
    deltaTime = js.Date.now - lastFrameTime
    lastFrameTime = js.Date.now
  }
  
  def loadLevel(mapData: String) {
    val positions = map.loadFromString(mapData)
    map.updateView()
    
    val playerPos = for {
      playerPositions <- positions.get(MapData.Tile.Player)
      player1Pos <- playerPositions.headOption
    } yield player1Pos
    playerPos match {
      case Some((x,z)) =>
        player.position.set(x, 0, z)
      case _ =>
        throw new Exception("Invalid map: no player position in map")
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
      monster.position.set(x, 0, z)
    }
  }
  
  def notifyVictory(score: Int) {
    println("victory! "+score)
  }
}