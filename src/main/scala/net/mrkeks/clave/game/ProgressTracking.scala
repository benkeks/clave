package net.mrkeks.clave.game

trait ProgressTracking {

  var score = 0

  val levelScores = collection.mutable.Map[String, Int]()

  var upcomingLevelId: Option[String] = None

  def unlockLevel(levelId: String) = {
    if (!levelScores.isDefinedAt(levelId)) levelScores(levelId) = 0
    upcomingLevelId = Some(levelId)
  }

  def bookScore(levelId: String, levelScore: Int) = {
    score += levelScore
    levelScores(levelId) = levelScore
  }
}
