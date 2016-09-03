package net.mrkeks.clave

import scala.scalajs.js
import scala.scalajs.js.Any.fromFunction0
import scala.scalajs.js.Any.fromFunction1
import scala.scalajs.js.Any.fromString
import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom
import net.mrkeks.clave.game.GameObject
import net.mrkeks.clave.game.Player
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.map.Maps
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.game.PlayerControl
import net.mrkeks.clave.game.Input
import net.mrkeks.clave.map.MapData

@JSExport
object Clave {
  
  val context = new DrawingContext()
  
  val input = new Input()
  
  /** List of individual objects in the game (movable stuff, enemies, the player..)  */
  var gameObjects = List[GameObject]()
  var gameObjectIdCount = 0
  
  var lastFrameTime = js.Date.now
  
  /** Time that passed since the last frame. (in ms) */
  var deltaTime = 0.0
  
  val map = new GameMap(16,16)
  add(map)
  
  val player = new Player(map)
  add(player)
  
  val playerControl = new PlayerControl(player, input)
  
  loadLevel(Maps.level0)
  
  def run() = {

  }

  @JSExport
  def main(): Unit = {

    dom.window.onkeypress = {(e: dom.KeyboardEvent) =>
//      if (e.keyCode.toInt == 32) bullets = player +: bullets
    }
    
    dom.window.setInterval(() => update(), 20)
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
    playerControl.update(deltaTime)
    
    gameObjects.foreach(_.update(deltaTime))
    
    context.render()
    
    deltaTime = js.Date.now - lastFrameTime
    lastFrameTime = js.Date.now
  }
  
  def loadLevel(mapData: String) {
    val positions = map.loadFromString(mapData)
    map.updateView()
    
    (for {
      playerPositions <- positions.get(MapData.Tile.Player)
      player1Pos <- playerPositions.headOption
    } yield player1Pos)
    match {
      case Some((x,z)) =>
        player.position.set(x, 0, z)
      case _ =>
        throw new Exception("Invalid map: no player position in map")
    }
  }
}
