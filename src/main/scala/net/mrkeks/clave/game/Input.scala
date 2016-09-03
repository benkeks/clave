package net.mrkeks.clave.game

import org.scalajs.dom

/** centrally manages the key board input */
class Input {
  val keysDown = collection.mutable.Set.empty[Int]
  
  dom.window.onkeydown = {(e: dom.KeyboardEvent) =>
    keysDown.add(e.keyCode.toInt)
  }
  dom.window.onkeyup = {(e: dom.KeyboardEvent) =>
    keysDown.remove(e.keyCode.toInt)
  }
}