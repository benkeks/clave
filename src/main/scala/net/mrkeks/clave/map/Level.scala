package net.mrkeks.clave.map

import net.mrkeks.clave.game.Game

object Level {

  case class ObjectInfo(kind: String, x: Int, z: Int, info: String)

  val level0 = Level(
      name = "Block 'em up",
      width = 16, height = 16,
      mapData = parseCSV(
    """0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0
       0, 1, 0, 1, 2, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0
       0, 1, 1, 0, 2, 1, 8, 1, 0, 2, 2, 1, 1, 0, 0, 0
       0, 1, 0, 1, 2, 1, 0, 1, 2, 0, 0, 1, 0, 1, 0, 0
       0, 1, 1, 0, 2, 2, 1, 0, 2, 0, 0, 1, 0, 1, 0, 0
       0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0
       1, 1, 1, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0
       1, 0, 0, 2, 2, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0
       1, 1, 0, 2, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0
       1, 0, 0, 2, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0
       1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 1, 0
       0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 2, 0, 2, 1, 0
       2, 2, 2, 0, 0, 0, 0, 0, 1, 0, 1, 2, 2, 0, 0, 0
       0, 0, 0, 9, 0, 1, 0, 0, 1, 0, 1, 2, 0, 0, 1, 0
       0, 0, 2, 0, 0, 0, 0, 0, 0, 1, 1, 2, 0, 0, 0, 0
       8, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0"""),
       objects = List(),
       version = 0)

  val titleScreen = Level(
      name = "TitleScreen",
      width = 21, height = 20,
      mapData = parseCSV(
     """0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0
        0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0
        0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 0, 0
        0, 1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0
        0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 1, 1, 0, 0, 1, 0, 0, 0, 1, 1, 0
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        0, 8, 6, 7, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 8, 0, 7, 0
        0, 0, 8, 0, 8, 0, 2, 0, 0, 0, 2, 0, 2, 0, 0, 0, 7, 0, 8, 0, 0
        0, 0, 0, 0, 0, 0, 2, 2, 2, 0, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0
        0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0
        0, 7, 0, 0, 0, 0, 2, 2, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        0, 0, 0, 4, 0, 4, 0, 0, 4, 4, 0, 4, 0, 4, 0, 0, 4, 4, 0, 7, 0
        0, 0, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 0, 0, 0, 0
        0, 0, 0, 4, 4, 0, 0, 4, 4, 0, 0, 4, 4, 0, 0, 0, 4, 0, 0, 0, 0
        0, 0, 0, 4, 0, 4, 0, 4, 0, 0, 0, 4, 0, 4, 0, 0, 0, 4, 0, 0, 0
        0, 0, 0, 4, 0, 4, 0, 0, 4, 4, 0, 4, 0, 4, 0, 4, 4, 0, 0, 0, 0
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        0, 0, 0,10,10,10, 0,10,10,10, 0,10,10,10, 0,10,10,10, 0, 9, 0"""),
       objects = List(),
       version = 0)

  def parseCSV(csv: String): Array[Array[Int]] = {
    csv
      .split("\n")              // split by lines
      .map(_.split(","))        // split by columns
      .map(_.map(_.trim.toInt)) // convert to int
  }

  def levelFromYAML(txt: String): Option[Level] = {
    val yaml = yamlesque.read(txt.drop(3)).obj
    if (Set("name", "width", "height", "tilemap") subsetOf yaml.keySet) {
      val objects = for {
        yObj <- yaml.get("objects").toList
        node <- yObj.arr
        nObj = node.obj
        info = nObj.get("info").flatMap(_.strOpt).getOrElse("")
      } yield Level.ObjectInfo( nObj("kind").str, nObj("x").str.toInt, nObj("z").str.toInt, info)
      val scorePerfect = yaml.get("score_perfect").map(_.str.toInt).getOrElse(3)
      val scoreOkay = yaml.get("score_okay").map(_.str.toInt).getOrElse(2)
      Some(Level(
        name = yaml("name").str,
        width = yaml("width").str.toInt,
        height = yaml("height").str.toInt,
        mapData = parseCSV(yaml("tilemap").str),
        objects = objects,
        scorePerfect = scorePerfect,
        scoreOkay = scoreOkay,
        scorePerfectHard = yaml.get("score_perfect_hard").map(_.str.toInt).getOrElse(scorePerfect),
        scoreOkayHard = yaml.get("score_okay_hard").map(_.str.toInt).getOrElse(scoreOkay),
        version = txt.hashCode()
      ))
    } else {
      None
    }
  }

  def levelNameListFromYAML(txt: String): List[String] = {
    val yaml = yamlesque.read(txt.drop(3)).obj
    yaml("levels").arr.toList.map(_.str)
  }
}

case class Level(
    name: String,
    width: Int,
    height: Int,
    mapData: Array[Array[Int]],
    objects: List[Level.ObjectInfo],
    scorePerfect: Int = 3,
    scoreOkay: Int = 2,
    scorePerfectHard: Int = 3,
    scoreOkayHard: Int = 2,
    version: Int = 0) {

  def gradeLevel(score: (Int, Int)): (Int, Int) = {
    val (easyScore, hardScore) = score
    (
      if (easyScore >= scorePerfect) 3 else if (easyScore >= scoreOkay) 2 else if (easyScore > 0) 1 else 0,
      if (hardScore >= scorePerfectHard) 3 else if (hardScore >= scoreOkayHard) 2 else if (hardScore > 0) 1 else 0
    )
  }

  def renderScore(score: (Int, Int)): String = {
    val (stars, skulls) = gradeLevel(score)
    val visibleStars = Math.max(stars - skulls, 0)
    val emptyStars = 3 - Math.max(skulls, stars)
    "🕱" * skulls + "★" * visibleStars + "☆" * emptyStars
  }

  def renderScoreForDifficulty(score: Int, difficulty: Game.Difficulty): String = {
    difficulty match {
      case Game.Difficulty.Easy =>
        val (stars, skulls) = gradeLevel((score, 0))
        "★" * stars + "☆" * (3 - stars)
      case Game.Difficulty.Hard => 
        val (stars, skulls) = gradeLevel((0, score))
        "🕱" * skulls + "☠" * (3 - skulls)
    }
  }
}