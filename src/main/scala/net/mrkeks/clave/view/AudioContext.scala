package net.mrkeks.clave.view

import net.mrkeks.clave.game.ProgressTracking
import net.mrkeks.clave.util.Mathf

import org.denigma.threejs.Audio
import org.denigma.threejs.AudioBuffer
import org.denigma.threejs.AudioLoader
import org.denigma.threejs.AudioListener

import scala.collection.mutable.HashMap
import scalajs.js
import org.scalajs.dom

class AudioContext(context: DrawingContext) {

  private val EffectVolumeKey = ProgressTracking.ClavePrefix+"effectVolume"
  private var effectVolume: Double = 0.0

  private val SoundDirectory = "sfx/"
  private val soundEffectFiles = Map(
    // ui
    "button-click" -> "Button_Click.wav",
    "button-hover" -> "Button_Hover.wav",
    "game-paused" -> "Game_Paused.wav",
    "game-unpaused" -> "Game_Unpaused.wav",
    "pause-atmosphere" -> "Pause_Atmosphere.wav",
    "level-won" -> "Level_Won.wav",
    "level-lost" -> "Level_Lost.wav",
    "victory-drawing" -> "Button_Hover.wav",
    // movement
    "player-moves" -> "Player_Moves.wav",
    "player-wall" -> "Player_Wall.wav",
    "player-crate" -> "Player_Crate.wav",
    "small-jumps" -> "Small_Jump.wav",
    "small-lands" -> "Small_Lands.wav",
    "big-jumps" -> "Big_Jump.wav",
    "big-lands" -> "Big_Lands.wav",
    "monster-spots" -> "Monster_Spots.wav",
    "monster-evades" -> "Monster_Evades.wav",
  )

  val soundEffects = new HashMap[String, AudioBuffer]()

  val audioListener = new AudioListener()

  val audioChannels = for (i <- 0 to 31) yield new Audio(audioListener)

  class AtmosphereAudio(audioListener: AudioListener) extends Audio(audioListener) {
    var fadeToVolume = 0.0
    var fadeSpeed = 0.0
    var volume = 0.0
  }

  val atmosphereChannels = HashMap[String, AtmosphereAudio]()

  private val loader = new AudioLoader()
  for ((key, file) <- soundEffectFiles.toList) {
    loader.load(SoundDirectory + file, soundEffects(key) = _)
  }
  context.camera.add(audioListener)
  
  def loadVolumeConfig(): Double = {
    val txt = dom.window.localStorage.getItem(EffectVolumeKey)
    if (txt != null && txt != "") {
      val volume = txt.toDoubleOption.getOrElse(0.0)
      setEffectVolume(volume)
      volume
    } else {
      setEffectVolume(effectVolume)
      effectVolume
    }
  }

  def setEffectVolumeConfig(volume: Double) = {
    dom.window.localStorage.setItem(EffectVolumeKey, volume.toString())
    setEffectVolume(volume)
  }

  def stopAll() = {
    for (ac <- audioChannels) {
      ac.stop()
    }
  }

  def update(deltaTime: Double) = {
    for {
      a <- atmosphereChannels.valuesIterator
      if a.isPlaying
    } {
      a.volume = Mathf.approach(a.volume, a.fadeToVolume, a.fadeSpeed * deltaTime)
      a.setVolume(a.volume)
    }
  }

  def play(key: String) = {
    if (effectVolume > 0) {
      for {
        buffer <- soundEffects.get(key)
        channel <- audioChannels.find(f => !f.isPlaying)
      } {
        channel.setBuffer(buffer)
        channel.play()
      }
    }
  }

  def playAtmosphere(key: String, targetVolume: Double = 1.0, fadeSpeed: Double = 1.0) = {
    for {
      buffer <- soundEffects.get(key)
      channel = atmosphereChannels.getOrElseUpdate(key, {
        val newChannel = new AtmosphereAudio(audioListener)
        newChannel.setBuffer(buffer)
        newChannel.setLoop(true)
        newChannel.setVolume(0.0)
        newChannel
      })
    } {
      if (!channel.isPlaying) {
        channel.play()
        if (fadeSpeed < .9) channel.setVolume(0.0)
      }
      channel.fadeToVolume = targetVolume
      channel.fadeSpeed = fadeSpeed
    }
  }

  def setAtmosphereVolume(key: String, volume: Double) = {
    for {
      channel <- atmosphereChannels.get(key)
    } {
      channel.volume = volume
      channel.fadeToVolume = volume
      if (volume <= 0) {
        channel.stop()
      }
    }
  }

  def setEffectVolume(volume: Double) = {
    effectVolume = volume
    audioListener.setMasterVolume(effectVolume)
  }

  def getEffectVolume(volume: Double) = {
    effectVolume
  }

}
