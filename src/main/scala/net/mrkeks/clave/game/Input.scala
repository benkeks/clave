package net.mrkeks.clave.game

import org.scalajs.dom
import scala.collection.mutable.MultiMap
import scala.collection.mutable.HashMap

/** centrally manages the key board input */
class Input {
  val keysDown = collection.mutable.Set.empty[Int]
  
  val keyPressListener: MultiMap[Int, (() => Unit)] =
    new HashMap[Int, collection.mutable.Set[() => Unit]]
      with MultiMap[Int, () => Unit]
  
  dom.window.onkeydown = {(e: dom.KeyboardEvent) =>
    keysDown.add(e.keyCode.toInt)
  }
  dom.window.onkeyup = {(e: dom.KeyboardEvent) =>
    keysDown.remove(e.keyCode.toInt)
  }
  
  dom.window.onkeypress = {(e: dom.KeyboardEvent) =>
    keyPressListener.get(e.keyCode.toInt)
      .foreach(_.foreach(cb => cb()))
  }
}