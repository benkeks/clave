package net.mrkeks.clave.game

import net.mrkeks.clave.game.objects.TriggerGroup
import net.mrkeks.clave.game.objects.Gate
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.game.objects.Crate
import net.mrkeks.clave.game.objects.Trigger
import net.mrkeks.clave.map.Level
import net.mrkeks.clave.map.MapData

trait GameLevelLoader {
  self: GameObjectManagement =>
    
  var levelId: Int
  var player: Player
  var map: GameMap
  
  def unloadLevel() {
    clear()
  }
  
  def loadLevel(id: Int) {
    levelId = id
    loadLevel(Level.levels(levelId))
  }
  
  def loadLevel(level: Level) {
    map = new GameMap(level.width, level.height)
    
    val positions = map.loadFromString(level.mapCsv)
    map.updateView()
    add(map)
    
    player = new Player(map)
    add(player)
    
    for {
      playerPositions <- positions.get(MapData.Tile.Player)
      (x, z) <- playerPositions.headOption
    } {
      player.setPosition(x, 0, z)
    }
    
    // for now add all triggers and gates to one big group for the whole level.
    val triggerGroup = new TriggerGroup
    add(triggerGroup)
    
    // add level elements
    def factoryConstruct(tileType: MapData.Tile) = tileType match {
      case MapData.Tile.Crate =>
        List(new Crate(map))
      case MapData.Tile.Monster =>
        List(new Monster(map))
      case MapData.Tile.GateOpen | MapData.Tile.GateClosed =>
        val gate = new Gate(map)
        triggerGroup.addGate(gate)
        List(gate)
      case MapData.Tile.Trigger =>
        val trigger = new Trigger(map)
        triggerGroup.addTrigger(trigger)
        List(trigger)
      case MapData.Tile.TriggerWithCrate =>
        val trigger = new Trigger(map)
        triggerGroup.addTrigger(trigger)
        val crate = new Crate(map)
        List(trigger, crate)
      case _ =>
        List()
    }
    
    positions.foreach { case (tileType: MapData.Tile, pos: List[(Int,Int)]) =>
      pos.foreach { case (x,z) =>
        factoryConstruct(tileType).foreach { obj =>
          obj.setPosition(x, 0, z)
          obj match {case c: Crate => c.place(x, z) case _ => }
          add(obj)
        }
      }
    }
  }
}