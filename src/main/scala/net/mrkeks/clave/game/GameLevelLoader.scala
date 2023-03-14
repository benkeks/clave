package net.mrkeks.clave.game

import net.mrkeks.clave.game.objects.TriggerGroup
import net.mrkeks.clave.game.objects.Gate
import net.mrkeks.clave.map.GameMap
import net.mrkeks.clave.game.objects.Crate
import net.mrkeks.clave.game.objects.Trigger
import net.mrkeks.clave.map.Level
import net.mrkeks.clave.map.MapData
import net.mrkeks.clave.game.characters.{Player, PlayerData}
import net.mrkeks.clave.game.characters.{Monster, MonsterData}
import net.mrkeks.clave.map.LevelDownloader
import net.mrkeks.clave.game.objects.CrateData
import net.mrkeks.clave.game.abstracts.GameObjectManagement
import net.mrkeks.clave.game.objects.Meta

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

  def loadLevelById(levelId: String, difficulty: Game.Difficulty = Game.Difficulty.Easy): Unit = {
    if (levelId == "__titleScreen__") {
      loadLevel(Level.titleScreen, difficulty)
      player.foreach(_.setPosition(10,0,20))
    } else {
      for (level <- levelDownloader.getLevelById(levelId)) {
        currentLevelNum = levelDownloader.getNumById(levelId)
        loadLevel(level, difficulty)
      }
    }
  }

  private def loadLevel(level: Level, difficulty: Game.Difficulty): Unit = {
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
      case MapData.Tile.DefensiveMonster => List("monster_defensive")
      case MapData.Tile.MonsterFriend => List("monster_friend")
      case MapData.Tile.GateOpen => List("gate_open")
      case MapData.Tile.GateClosed => List("gate_closed")
      case MapData.Tile.Trigger => List("trigger")
      case MapData.Tile.Freezer => List("crate_freezer")
      case _ => List()
    }

    // add level elements
    def factoryConstruct(objKind: String) = objKind match {
      case "crate" =>
        List(new Crate(map))
      case "crate_player" =>
        List(new Crate(map, kind = CrateData.PlayerLikeKind))
      case "crate_freezer" =>
        List(new Crate(map, kind = CrateData.FreezerKind(None)))
      case "monster" =>
        val monster = new Monster(map)
        monster.sizeLevel = difficulty match {
          case Game.Difficulty.Hard => 2
          case _ => 1
        }
        List(monster)
      case "monster_defensive" =>
        val monster = new Monster(map, kind = MonsterData.FrightenedMonster)
        monster.sizeLevel = difficulty match {
          case Game.Difficulty.Hard => 2
          case _ => 1
        }
        List(monster)
      case "monster_friend" =>
        List(new Monster(map, kind = MonsterData.FriendlyMonster))
      case "gate_open" | "gate_closed" =>
        val gate = new Gate(map)
        triggerGroup.addGate(gate)
        List(gate)
      case "trigger" =>
        val trigger = new Trigger(map)
        triggerGroup.addTrigger(trigger)
        List(trigger)
      case "meta" =>
        List(new Meta(map))
      case _ =>
        List()
    }

    val tileObjects = for {
      (tileType: MapData.Tile, pos: List[(Int,Int)]) <- positions
      (x,z) <- pos
      kind <- objKindsFromTile(tileType)
    } yield Level.ObjectInfo(kind, x, z, "")

    for {
      Level.ObjectInfo(kind, x, z, info) <-
        tileObjects ++
        level.objects ++
        (difficulty match { case Game.Difficulty.Hard => level.objectsHard case _ => List() })
      obj <- factoryConstruct(kind)
    } {
      obj.setPosition(x, 0, z)
      obj.setInfo(info)
      obj match { case c: Crate => c.place(x, z); case _ => }
      add(obj)
    }

    map.updateView()
    add(map)
  }
}