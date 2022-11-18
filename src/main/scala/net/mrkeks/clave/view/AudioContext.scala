package net.mrkeks.clave.view

import org.denigma.threejs.Audio
import org.denigma.threejs.AudioBuffer
import org.denigma.threejs.AudioLoader
import org.denigma.threejs.AudioListener

import scala.collection.mutable.HashMap
import scalajs.js
import org.scalajs.dom

class AudioContext(context: DrawingContext) {

  private val SoundDirectory = "sfx/"
  private val soundEffectFiles = Map(
    "button-click" -> "Button_Click.wav",
    "button-hover" -> "Button_Hover.wav",
    "game-paused" -> "Game_Paused.wav",
    "game-unpaused" -> "Game_Unpaused.wav",
    "level-won" -> "Level_Won.wav",
    "victory-drawing" -> "Button_Hover.wav",
  )
  val soundEffects = new HashMap[String, AudioBuffer]()

  val audioListener = new AudioListener()

  val audioChannels = for (i <- 0 to 31) yield new Audio(audioListener)

  private val loader = new AudioLoader()
  for ((key, file) <- soundEffectFiles.toList) {
    loader.load(SoundDirectory + file, soundEffects(key) = _)
  }
  context.camera.add(audioListener)

  def stopAll() = {
    for (ac <- audioChannels) {
      ac.stop()
    }
  }

  def play(key: String) = {
    //dom.window.console.log(audioListener.asInstanceOf[js.Dynamic].context)
    for {
      buffer <- soundEffects.get(key)
      channel <- audioChannels.find(f => !f.isPlaying)
    } {
      println("play "+key)
      channel.setBuffer(buffer)
      channel.play()
    }
  }

}
