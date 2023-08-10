package net.mrkeks.clave.game

import org.scalajs.dom
import net.mrkeks.clave.map.LevelDownloader

object ProgressTracking {
  val ClavePrefix = "clave."
  val ClaveVersion = "0.4.2"
  val LocalStorageScoreKey = ClavePrefix + "scores"
}

trait ProgressTracking {
  import ProgressTracking._

  val levelDownloader: LevelDownloader

  var wonLevels = 0

  val levelScores = collection.mutable.LinkedHashMap[String, (Int, Int)]()
  var initialScores = collection.mutable.LinkedHashMap[String, (Int, Int)]()

  var upcomingLevelId: Option[String] = None

  def unlockLevel(levelId: String): Unit = {
    if (!levelScores.isDefinedAt(levelId)) levelScores(levelId) = (0, 0)
    upcomingLevelId = Some(levelId)
    saveProgress()
  }

  private def updateWonLevels(): Unit = {
    wonLevels = levelScores.count(s => s._2._1 > 0 || s._2._2 > 0)
  }

  def bookScore(levelId: String, levelScore: Int, difficulty: Game.Difficulty): Unit = {
    val newEasyScore = if (difficulty == Game.Difficulty.Easy) levelScore else 0
    val newHardScore = if (difficulty == Game.Difficulty.Hard) levelScore else 0
    levelScores.updateWith(levelId) {
      case None => Some(newEasyScore, newHardScore)
      case Some((easyScore, hardScore)) => Some(Math.max(easyScore, newEasyScore), Math.max(hardScore, newHardScore))
    }
    updateWonLevels()
    saveProgress()
  }

  def getScoreForDifficulty(levelId: String, difficulty: Game.Difficulty): Int = {
    val (levelScoreEasy, levelScoreHard) = levelScores.get(levelId).getOrElse((0,0))
    difficulty match {
      case Game.Difficulty.Easy => levelScoreEasy
      case Game.Difficulty.Hard => levelScoreHard
    }
  }

  def scoreHasBeenUpdated(levelId: String): Boolean = {
    levelScores.get(levelId).exists(newScore => initialScores.get(levelId).forall(oldScore => oldScore._1 < newScore._1 || oldScore._2 < newScore._2))
  }

  private def saveProgress(): Unit = {
    val scoresYaml = for {
      (lvlId, lvlScore) <- levelScores
      lvl <- levelDownloader.getLevelById(lvlId)
      scoreEntry = yamlesque.Obj(
        ("easy", new yamlesque.Str(lvlScore._1.toString())),
        ("hard", new yamlesque.Str(lvlScore._2.toString())))
    } yield (s"$lvlId+${lvl.version}", scoreEntry.asInstanceOf[yamlesque.Value])
    val scoreMap = new yamlesque.Obj(scoresYaml)
    val yamlString = yamlesque.write(yamlesque.Obj(
      "scores" -> scoreMap,
      "version" -> yamlesque.Str(ClaveVersion),
      "hash" -> yamlesque.Str(levelScores.hashCode().toString())))
    dom.window.localStorage.setItem(LocalStorageScoreKey, yamlString)
  }

  def loadProgress(): Unit = {
    val txt = dom.window.localStorage.getItem(LocalStorageScoreKey)
    if (txt != null && txt != "") {
      val yaml = yamlesque.read(txt).obj
      if ((Set("scores", "version", "hash") subsetOf yaml.keySet)) {
        val loadedScores = for {
          yScores <- yaml.get("scores").toList
          (lvlIdPlusHash, yamlesque.Obj(lvlScore)) <- yScores.obj
          lvlIdParsed = lvlIdPlusHash.split('+')
          if lvlIdParsed.length == 2
          if lvlScore.contains("easy") && lvlScore.contains("hard")
          levelScore =
            if (levelDownloader.getLevelById(lvlIdParsed(0)).exists(l => l.version.toString == lvlIdParsed(1)))
              (lvlScore("easy").str.toInt, lvlScore("hard").str.toInt)
            else
              (0,0)
        } yield (lvlIdParsed(0), levelScore)
        levelScores.clear()
        levelScores.addAll(loadedScores)
        initialScores = levelScores.clone()
        updateWonLevels()
      } else {
        // invalid entry in local storage! discard it to be overwritten soon!
        dom.window.localStorage.removeItem(LocalStorageScoreKey)
      }
    }
  }

  def hardModeAvailable() = {
    // for now, activate hard mode right from the start.
    wonLevels >= 0
  }
}
