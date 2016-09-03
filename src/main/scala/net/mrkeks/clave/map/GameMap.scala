package net.mrkeks.clave.map

import net.mrkeks.clave.view.DrawingContext

class GameMap(val width: Int, val height: Int) {
  
  object Tile extends Enumeration {
    val Empty, Wall, SolidWall, Something3, Something4,
        Something5, Something6, Something7, EmptyMonster, EmptyPlayer = Value
  }
  type Tile = Tile.Value
  
  private val data = Array.ofDim[Tile](width, height)
  
  def loadFromString(strData: String) {
    val rawArray = strData
      .split("\n")
      .map(_.split(","))
      .map(_.map(_.trim.toInt))
      
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        data(x)(y) = Tile(rawArray(y)(x))
      }
    }
    println(data(0).map(_.toString).mkString(","))
  }
  
  def draw(context: DrawingContext) {
    for (x <- 0 until width) {
      for (y <- 0 until height) {
        data(x)(y) match {
          case Tile.Wall =>
            context.drawBox(x, y)
          case _ =>
        }
      }
    }
  }
}

