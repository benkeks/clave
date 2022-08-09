package net.mrkeks.clave.view

import org.scalajs.dom
import scala.collection.mutable.MultiDict
import scala.scalajs.js.Any.fromFunction1

/** centrally manages the key board input */
class Input {

  val keysDown = collection.mutable.Set.empty[Int]

  val keyPressListener = MultiDict[String, (AnyRef, () => Unit)]()

  dom.window.onkeydown = { e: dom.KeyboardEvent =>
    keysDown.add(toKeyCodeInt(e))
  }
  dom.window.onkeyup = {e: dom.KeyboardEvent =>
    keysDown.remove(toKeyCodeInt(e))
  }
  
  dom.window.onkeypress = {(e: dom.KeyboardEvent) =>
    keyPressListener.get(e.key)
      .foreach {  case (_, cb) => cb() }
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

      if (diffTime > Input.MovementTouchTime && (touch.changedDirection || length > Input.MovementTouchLengthThreshold)) {
        
        if (diffX < -Input.MovementTouchDirectionThreshold * length) {
          keysDown.add(PlayerControl.LeftCode)
          touch.changedDirection = true
        } else {
          keysDown.remove(PlayerControl.LeftCode)
        }

        if (diffX > Input.MovementTouchDirectionThreshold * length) {
          keysDown.add(PlayerControl.RightCode)
          touch.changedDirection = true
        } else {
          keysDown.remove(PlayerControl.RightCode)
        }

        if (diffY < -Input.MovementTouchDirectionThreshold * length) {
          keysDown.add(PlayerControl.UpCode)
          touch.changedDirection = true
        } else {
          keysDown.remove(PlayerControl.UpCode)
        }

        if (diffY > Input.MovementTouchDirectionThreshold * length) {
          keysDown.add(40)
          touch.changedDirection = true
        } else {
          keysDown.remove(PlayerControl.DownCode)
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
        keysDown.remove(PlayerControl.LeftCode)
        keysDown.remove(PlayerControl.RightCode)
        keysDown.remove(PlayerControl.UpCode)
        keysDown.remove(PlayerControl.DownCode)
      }
      if (!t.changedDirection && e.timeStamp - t.start < 500) {
        fakeAction(PlayerControl.ActionCharStr)
      }
    }
  })

  private def fakeAction(keyStr: String) = {
    keyPressListener.get(keyStr)
      .foreach {  case (_, cb) => cb() }
  }

  var gamepadsActive = false
  var gamepad: Option[dom.experimental.gamepad.Gamepad] = None

  def updateGamepads() = {
    if (gamepadsActive) {
      val gamepadNavigator = dom.experimental.gamepad.toGamepad(dom.window.navigator)
      val gamepads = gamepadNavigator.getGamepads()
      val oldGamepad = gamepad
      gamepad = gamepads.headOption
      if (gamepad == Some(null)) gamepad = None

      if (oldGamepad.exists(gp => !gp.buttons(0).pressed) && gamepad.exists(_.buttons(0).pressed)) {
        fakeAction(PlayerControl.ActionCharStr)
      }
    } else {
      gamepad = None
    }
  }

  private def toggleGamepads(ev: dom.Event) = {
    val gamepadNavigator = dom.experimental.gamepad.toGamepad(dom.window.navigator)
    gamepadsActive = gamepadNavigator.getGamepads().nonEmpty
  }

  dom.window.addEventListener("gamepadconnected", toggleGamepads(_))
  dom.window.addEventListener("gamepaddisconnected", toggleGamepads(_))

  private def toKeyCodeInt(e: dom.KeyboardEvent) = {
    if (e.charCode == PlayerControl.ActionChar.toInt) {
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
      keysDown.remove(PlayerControl.LeftCode)
      keysDown.remove(PlayerControl.RightCode)
      keysDown.remove(PlayerControl.UpCode)
      keysDown.remove(PlayerControl.DownCode)
    }
    updateGamepads()
  }
}

object Input {
  
  val MovementTouchTime: Double = 30

  val MovementTouchDirectionThreshold: Double = .6

  val MovementTouchLengthThreshold: Double = 22
}