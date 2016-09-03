package net.mrkeks.clave.view

import org.scalajs.dom.raw.CanvasRenderingContext2D
import org.scalajs.dom.html.Canvas

class DrawingContext(canvas: Canvas) {
  
  val renderingContext =
    canvas.getContext("2d")
          .asInstanceOf[CanvasRenderingContext2D]
  
  val xScale: Double = canvas.width / 1024.0 
  val yScale: Double = canvas.height / 1024.0
  
  def drawBox(x: Double, y: Double) {
    renderingContext.fillStyle = "#675"
    renderingContext.fillRect(x * xScale * 1024 / 16.0, y * yScale * 1024 / 16.0, 
        xScale * 1024 / 16, yScale * 1024 / 16)
  }
}