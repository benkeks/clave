package net.mrkeks.clave.view

import org.scalajs.dom
import scala.collection.mutable.MultiMap
import scala.collection.mutable.HashMap
import scala.scalajs.js.Any.fromFunction1

/** centrally manages the key board input */
class Input {
  val keysDown = collection.mutable.Set.empty[Int]
  
  val keyPressListener: MultiMap[String, (() => Unit)] =
    new HashMap[String, collection.mutable.Set[() => Unit]]
      with MultiMap[String, () => Unit]
  
  dom.window.onkeydown = { e: dom.KeyboardEvent =>
    keysDown.add(toKeyCodeInt(e))
  }
  dom.window.onkeyup = {e: dom.KeyboardEvent =>
    keysDown.remove(toKeyCodeInt(e))
  }
  
  dom.window.onkeypress = {(e: dom.KeyboardEvent) =>
    keyPressListener.get(e.key)
      .foreach(_.foreach(cb => cb()))
  }

  class Touch(
    val start: Double,
    var lastTime: Double,
    var lastX: Double,
    var lastY: Double,
    var changedDirection: Boolean = false
  )

  val touches = collection.mutable.HashMap[Double, Touch]()

  dom.window.addEventListener("touchstart", { (e: dom.TouchEvent) => 
    e.preventDefault()
    val domTouch = e.changedTouches(0)
    touches += ((domTouch.identifier, new Touch(e.timeStamp, e.timeStamp, domTouch.clientX, domTouch.clientY)))
  })
  
  dom.window.addEventListener("touchmove", { (e: dom.TouchEvent) => 
    
    for {
      i <- (0 to e.touches.length - 1)
      domTouch = e.changedTouches(i)
      touch <- touches.get(domTouch.identifier)
    } {
      val diffX = domTouch.clientX - touch.lastX
      val diffY = domTouch.clientY - touch.lastY
      val diffTime = e.timeStamp - touch.lastTime
      val length = scala.math.sqrt(diffX * diffX + diffY * diffY)

      if (diffTime > Input.MovementTouchTime && (touch.changedDirection || length >  Input.MovementTouchLengthThreshold)) {
        
        if (diffX < -Input.MovementTouchDirectionThreshold * length) {
          keysDown.add(37)
          touch.changedDirection = true
        } else {
          keysDown.remove(37)
        }

        if (diffX > Input.MovementTouchDirectionThreshold * length) {
          keysDown.add(39)
          touch.changedDirection = true
        } else {
          keysDown.remove(39)
        }

        if (diffY < -Input.MovementTouchDirectionThreshold * length) {
          keysDown.add(38)
          touch.changedDirection = true
        } else {
          keysDown.remove(38)
        }

        if (diffY > Input.MovementTouchDirectionThreshold * length) {
          keysDown.add(40)
          touch.changedDirection = true
        } else {
          keysDown.remove(40)
        }

        touch.lastX = domTouch.clientX
        touch.lastY = domTouch.clientY
        touch.lastTime = e.timeStamp
      }
    }

  })

  dom.window.addEventListener("touchend", { (e: dom.TouchEvent) => 
    for (t <- touches.remove(e.changedTouches(0).identifier)) {
      if (t.changedDirection) {
        keysDown.remove(37)
        keysDown.remove(39)
        keysDown.remove(38)
        keysDown.remove(40)
      }
      if (!t.changedDirection && e.timeStamp - t.start < 500) {
        keyPressListener.get(" ")
          .foreach(_.foreach(cb => cb()))
      }
    }
  })

  private def toKeyCodeInt(e: dom.KeyboardEvent) = {
    if (e.charCode == ' '.toInt) {
      32
    } else {
      e.keyCode.toInt
    }
  }

  def update(timeStamp: Double) = {
    for {
      (tId, touch) <- touches
      if touch.changedDirection
      if timeStamp - touch.lastTime > 10 * Input.MovementTouchTime
    } {
      keysDown.remove(37)
      keysDown.remove(39)
      keysDown.remove(38)
      keysDown.remove(40)
    }
  }
}

object Input {
  
  val MovementTouchTime: Double = 30

  val MovementTouchDirectionThreshold: Double = .5

  val MovementTouchLengthThreshold: Double = 20
}