package net.mrkeks.clave.game.abstracts

import net.mrkeks.clave.view.DrawingContext

trait GameObject extends GameObjectData {
  
  def init(context: DrawingContext): Unit
  
  def clear(context: DrawingContext): Unit
  
  def update(deltaTime: Double): Unit
}