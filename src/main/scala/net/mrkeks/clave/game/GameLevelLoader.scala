package net.mrkeks.clave.game

import net.mrkeks.clave.game.objects.TriggerGroup
import net.mrkeks.clave.game.objects.Gate
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.game.objects.Crate
import net.mrkeks.clave.game.objects.Trigger
import net.mrkeks.clave.map.Level
import net.mrkeks.clave.map.MapData
import net.mrkeks.clave.game.characters.{Player, PlayerData}
import net.mrkeks.clave.game.characters.Monster
import net.mrkeks.clave.map.LevelDownloader
import net.mrkeks.clave.game.objects.CrateData
import net.mrkeks.clave.game.abstracts.GameObjectManagement

trait GameLevelLoader {
  self: GameObjectManagement =>

  var player: Option[Player]
  var map: GameMap
  val levelDownloader: LevelDownloader

  var currentLevelNum: Int = 0
  def currentLevelId: String = levelDownloader.getLevelIdByNum(currentLevelNum)
  var currentLevel: Option[Level] = None

  def unloadLevel(): Unit = {
    clear()
  }

  def loadLevelById(levelId: String): Unit = {
    if (levelId == "__titleScreen__") {
      loadLevel(Level.titleScreen)
      player.foreach(_.setPosition(10,0,20))
    } else {
      for (level <- levelDownloader.getLevelById(levelId)) {
        currentLevelNum = levelDownloader.getNumById(levelId)
        loadLevel(level)
      }
    }
  }

  private def loadLevel(level: Level): Unit = {
    currentLevel = Some(level)
    map = new GameMap(level.width, level.height)

    context.adjustCameraForMap(level.width, level.height)

    val positions = map.loadFromArray(level.mapData)

    val newPlayer = new Player(map)
    add(newPlayer)
    player = Some(newPlayer)
    
    for {
      playerPositions <- positions.get(MapData.Tile.Player)
      (x, z) <- playerPositions.headOption
    } {
      newPlayer.setPosition(x, 400, z)
    }
    
    // for now add all triggers and gates to one big group for the whole level.
    val triggerGroup = new TriggerGroup
    add(triggerGroup)

    def objKindsFromTile(tileType: MapData.Tile) = tileType match {
      case MapData.Tile.Crate => List("crate")
      case MapData.Tile.Monster => List("monster")
      case MapData.Tile.GateOpen => List("gate_open")
      case MapData.Tile.GateClosed => List("gate_closed")
      case MapData.Tile.Trigger => List("trigger")
      case MapData.Tile.TriggerWithCrate => List("trigger", "crate")
      case _ => List()
    }

    // add level elements
    def factoryConstruct(objKind: String) = objKind match {
      case "crate" =>
        List(new Crate(map))
      case "crate_player" =>
        List(new Crate(map, kind = CrateData.PlayerLikeKind))
      case "monster" =>
        List(new Monster(map))
      case "gate_open" | "gate_closed" =>
        val gate = new Gate(map)
        triggerGroup.addGate(gate)
        List(gate)
      case "trigger" =>
        val trigger = new Trigger(map)
        triggerGroup.addTrigger(trigger)
        List(trigger)
      case _ =>
        List()
    }

    val tileObjects = for {
      (tileType: MapData.Tile, pos: List[(Int,Int)]) <- positions
      (x,z) <- pos
      kind <- objKindsFromTile(tileType)
    } yield Level.ObjectInfo(kind, x, z)

    for {
      Level.ObjectInfo(kind, x, z) <- tileObjects ++ level.objects
      obj <- factoryConstruct(kind)
    } {
      obj.setPosition(x, 0, z)
      obj match {case c: Crate => c.place(x, z) case _ => }
      add(obj)
    }

    map.updateView()
    add(map)
  }
}