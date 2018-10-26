package net.mrkeks.clave.view

import org.scalajs.dom
import scala.collection.mutable.MultiMap
import scala.collection.mutable.HashMap
import scala.scalajs.js.Any.fromFunction1

/** centrally manages the key board input */
class Input {
  val keysDown = collection.mutable.Set.empty[Int]
  
  val keyPressListener: MultiMap[Int, (() => Unit)] =
    new HashMap[Int, collection.mutable.Set[() => Unit]]
      with MultiMap[Int, () => Unit]
  
  dom.window.onkeydown = { e: dom.KeyboardEvent =>
    keysDown.add(toKeyCodeInt(e))
  }
  dom.window.onkeyup = {e: dom.KeyboardEvent =>
    keysDown.remove(toKeyCodeInt(e))
  }
  
  dom.window.onkeypress = {e: dom.KeyboardEvent =>
    keyPressListener.get(toKeyCodeInt(e))
      .foreach(_.foreach(cb => cb()))
  }

  private def toKeyCodeInt(e: dom.KeyboardEvent) = {
    if (e.charCode == ' '.toInt) {
      32
    } else {
      e.keyCode.toInt
    }
  }
}