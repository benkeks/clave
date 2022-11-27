package net.mrkeks.clave.game

import org.scalajs.dom

object ProgressTracking {
  val ClavePrefix = "clave."
  val ClaveVersion = "0.3.0"
  val LocalStorageScoreKey = ClavePrefix + "scores"
}

trait ProgressTracking {
  import ProgressTracking._

  var score = 0

  val levelScores = collection.mutable.LinkedHashMap[String, Int]()
  var initialScores = collection.mutable.LinkedHashMap[String, Int]()

  var upcomingLevelId: Option[String] = None

  def unlockLevel(levelId: String): Unit = {
    if (!levelScores.isDefinedAt(levelId)) levelScores(levelId) = 0
    upcomingLevelId = Some(levelId)
    saveProgress()
  }

  def bookScore(levelId: String, levelScore: Int): Unit = {
    score += levelScore
    levelScores.updateWith(levelId)(_.orElse(Some(0)).map(Math.max(_, levelScore)))
    saveProgress()
  }

  def scoreHasBeenUpdated(levelId: String): Boolean = {
    levelScores.get(levelId).exists(newScore => initialScores.get(levelId).forall(newScore > _))
  }

  private def saveProgress(): Unit = {
    val scoresYaml = for {
      (lvlId, lvlScore) <- levelScores
    } yield (lvlId, new yamlesque.Str(lvlScore.toString()).asInstanceOf[yamlesque.Value])
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
      if ((Set("scores", "version", "hash") subsetOf yaml.keySet)
        && yaml("version").str == ClaveVersion) {
        val loadedScores = for {
          yScores <- yaml.get("scores").toList
          (lvlId, yamlesque.Str(lvlScore)) <- yScores.obj
        } yield (lvlId, lvlScore.toInt)
        levelScores.clear()
        levelScores.addAll(loadedScores)
        initialScores = levelScores.clone()
      } else {
        // invalid entry in local storage! discard it to be overwritten soon!
        dom.window.localStorage.removeItem(LocalStorageScoreKey)
      }
    }
  }
}
