package net.mrkeks.clave

import org.scalajs.dom
import org.scalajs.dom.html
import scala.scalajs.js.annotation.JSExport
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.map.Maps
import net.mrkeks.clave.view.DrawingContext

case class Point(x: Double, y: Double){
  def +(p: Point) = Point(x + p.x, y + p.y)
  def -(p: Point) = Point(x - p.x, y - p.y)
  def /(d: Double) = Point(x / d, y / d)
  def *(d: Double) = Point(x * d, y * d)
  def length = Math.sqrt(x * x + y * y)
}

@JSExport
object Clave {
  
  val context = new DrawingContext()

  var count = 0
  var player = Point(dom.window.innerWidth / 2.0,
                     dom.window.innerHeight / 2.0)
  val corners = Seq(Point(255, 255), Point(0, 255), Point(128, 0))

  var bullets = Seq.empty[Point]
  var enemies = Seq.empty[Point]

  var wave = 1
  
  val map = new GameMap(16,16)
  map.loadFromString(Maps.level0)
  map.init(context)
  map.updateView()
  
  def run() = {
    count += 1
    bullets = bullets.map(
      p => Point(p.x, p.y - 5)
    )

//    if (enemies.isEmpty){
//      enemies = for{
//        x <- (0 until canvas.width.toInt by 50)
//        y <- 0 until wave
//      } yield {
//        Point(x, 50 + y * 50)
//      }
//      wave += 1
//    }

    enemies = enemies.filter( e =>
      !bullets.exists(b =>
        (e - b).length < 5
      )
    )
    enemies = enemies.map{ e =>
      val i = count % 200
      if (i < 50) e.copy(x = e.x - 0.2)
      else if (i < 100) e.copy(y = e.y + 0.2)
      else if (i < 150) e.copy(x = e.x + 0.2)
      else e.copy(y = e.y + 0.2)
    }


    if (keysDown(38)) player += Point(0, -2)
    if (keysDown(37)) player += Point(-2, 0)
    if (keysDown(39)) player += Point(2, 0)
    if (keysDown(40)) player += Point(0, 2)
  }

  def draw() = {
//    ctx.fillStyle = "black"
//    ctx.fillRect(0, 0, canvas.width, canvas.height)
//
//    map.draw(context)
//    
//    ctx.fillStyle = "white"
//    ctx.fillRect(player.x - 5, player.y - 5, 10, 10)
//
//    ctx.fillStyle = "yellow"
//    for (enemy <- enemies){
//      ctx.fillRect(enemy.x - 5, enemy.y - 5, 10, 10)
//    }
//    ctx.fillStyle = "red"
//    for (bullet <- bullets){
//      ctx.fillRect(bullet.x - 2, bullet.y - 2, 4, 4)
//    }
  }

  val keysDown = collection.mutable.Set.empty[Int]
  @JSExport
  def main(): Unit = {

    dom.console.log("main")
    dom.window.onkeypress = {(e: dom.KeyboardEvent) =>
      if (e.keyCode.toInt == 32) bullets = player +: bullets
    }
    dom.window.onkeydown = {(e: dom.KeyboardEvent) =>
      keysDown.add(e.keyCode.toInt)
    }
    dom.window.onkeyup = {(e: dom.KeyboardEvent) =>
      keysDown.remove(e.keyCode.toInt)
    }
    dom.window.setInterval(() => {run(); context.render()}, 20)
  }
}
