package net.mrkeks.clave.game

import net.mrkeks.clave.view.DrawingContext

trait GameObjectManagement {
  
  val context: DrawingContext
  
  /** List of individual objects in the game (movable stuff, enemies, the player..)  */
  var gameObjects = List[GameObject]()
  var gameObjectIdCount = 0
  
  def add(o: GameObject): Unit = {
    gameObjects = o :: gameObjects
    gameObjectIdCount += 1
    o.id = gameObjectIdCount
    o.init(context)
  }
  
  def remove(o: GameObject): Unit = {
    gameObjects = gameObjects.filterNot(_.id == o.id)
    o.clear(context)
  }
  
  def clear(): Unit = {
    gameObjects.foreach(remove)
  }
}