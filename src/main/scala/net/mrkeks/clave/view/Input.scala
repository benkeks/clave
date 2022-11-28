package net.mrkeks.clave.view

import org.scalajs.dom
import scala.collection.mutable.Queue
import scala.scalajs.js.Any.fromFunction1
import net.mrkeks.clave.view.Input.UnknownInput
import net.mrkeks.clave.view.Input.KeyboardInput
import net.mrkeks.clave.view.Input.TouchInput
import net.mrkeks.clave.view.Input.GamepadInput

/** centrally manages the keyboard input */
class Input {

  val keysDown = collection.mutable.Set.empty[Int]

  var mainInputMode: Input.InputMode = Input.UnknownInput

  var actionKeyListeners = new Queue[Input.ActionKeyListener]()
  var menuKeyListeners = new Queue[Input.MenuKeyListener]()

  dom.window.onkeydown = { e: dom.KeyboardEvent =>
    detectInputMode(Input.KeyboardInput)
    keysDown.add(toKeyCodeInt(e))
    if (e.key == PlayerControl.ActionCharStr) {
      triggerAction()
    } else if (e.key == Input.MenuKeyStr) {
      triggerMenu()
    }
  }
  dom.window.onkeyup = {e: dom.KeyboardEvent =>
    keysDown.remove(toKeyCodeInt(e))
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
    detectInputMode(Input.TouchInput)

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
        triggerAction()
      }
    }
  })

  private def triggerAction() = {
    actionKeyListeners.foreach(_.handleActionKey())
  }

  private def triggerMenu() = {
    menuKeyListeners.foreach(_.handleMenuKey())
  }

  private def detectInputMode(mode: Input.InputMode) = {
    if (mainInputMode == Input.UnknownInput) {
      mainInputMode = mode
    }
  }

  var gamepadsActive = false
  var gamepad: Option[dom.Gamepad] = None

  def updateGamepads() = {
    if (gamepadsActive) {
      val gamepads = dom.window.navigator.getGamepads()
      val oldGamepad = gamepad
      gamepad = gamepads.headOption
      if (gamepad == Some(null)) gamepad = None
      if (oldGamepad.exists(gp => !gp.buttons(0).pressed) && gamepad.exists(_.buttons(0).pressed)) {
        triggerAction()
      }
      if (oldGamepad.exists(gp => !gp.buttons(9).pressed) && gamepad.exists(_.buttons(9).pressed)) {
        triggerMenu()
      }
    } else {
      gamepad = None
    }
  }

  private def toggleGamepads(ev: dom.Event) = {
    gamepadsActive = dom.window.navigator.getGamepads().nonEmpty
    if (gamepadsActive) {
      detectInputMode(Input.GamepadInput)
    }
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

  def renderInputHint(txt: String): String = {
    txt
      .replace("$DoAction", mainInputMode match {
        case UnknownInput => "Press [Space] or (a) or tap"
        case KeyboardInput => "Press [Space]"
        case TouchInput => "Tap"
        case GamepadInput => "Press gamepad (a)"
      })
      .replace("$DoNavigate", mainInputMode match {
        case UnknownInput => "Use arrow key / joystick / swiping"
        case KeyboardInput => "Use arrow keys"
        case TouchInput => "Swipe anywhere"
        case GamepadInput => "Use left joystick"
      })
  }
}

object Input {
  
  val MovementTouchTime: Double = 30

  val MovementTouchDirectionThreshold: Double = .6

  val MovementTouchLengthThreshold: Double = 22

  val MenuKeyStr = "Escape"

  abstract sealed class InputMode
  case object UnknownInput extends InputMode
  case object KeyboardInput extends InputMode
  case object TouchInput extends InputMode
  case object GamepadInput extends InputMode

  trait ActionKeyListener {
    def handleActionKey(): Unit
  }

  trait MenuKeyListener {
    def handleMenuKey(): Unit
  }
}