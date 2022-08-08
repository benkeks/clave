package net.mrkeks.clave.game.abstracts

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

  def removeAllMarkedForDeletion(): Unit = {
    if (gameObjects.exists(_.markedForDeletion)) { // only rebuild the list if necessary
      val (deleted, surviving) = gameObjects.partition(_.markedForDeletion)
      deleted.foreach(_.clear(context))
      gameObjects = surviving
    }
  }

  def clear(): Unit = {
    gameObjects.foreach(remove)
  }
}