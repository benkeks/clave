package net.mrkeks.clave.map.game

import net.mrkeks.clave.view.DrawingContext

trait GameObject {
  def init(context: DrawingContext)
  
  def clear()
}