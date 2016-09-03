package net.mrkeks.clave.game

import net.mrkeks.clave.view.DrawingContext

trait GameObject extends GameObjectData {
  
  def init(context: DrawingContext)
  
  def clear()
  
  def update(deltaTime: Double)
  
}