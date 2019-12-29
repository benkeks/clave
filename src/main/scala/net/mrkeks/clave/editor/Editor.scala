package net.mrkeks.clave.editor

import net.mrkeks.clave.util.TimeManagement
import net.mrkeks.clave.view.DrawingContext
import net.mrkeks.clave.view.Input
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.map.MapData
import net.mrkeks.clave.view.GUI
import net.mrkeks.clave.map.Level
import net.mrkeks.clave.game.GameObjectManagement
import net.mrkeks.clave.game.GameLevelLoader
import net.mrkeks.clave.game.objects.Crate
import net.mrkeks.clave.game.objects.Gate
import net.mrkeks.clave.game.objects.Trigger
import net.mrkeks.clave.game.objects.TriggerGroup
import net.mrkeks.clave.game.characters.Player
import net.mrkeks.clave.game.characters.PlayerData

class Editor(val context: DrawingContext, val input: Input, val gui: GUI)
  extends GameObjectManagement with GameLevelLoader with TimeManagement {
  
  var levelId: Int = 0
  
  var player: Player = null

  var map: GameMap = null
  
  def update(timeStamp: Double): Unit = {

    input.update(timeStamp)

    context.render()

    updateTime(timeStamp)
  }

}