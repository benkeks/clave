package net.mrkeks.clave.map

import org.scalajs.dom.experimental.Fetch
import scala.scalajs.js.Thenable.Implicits._
import scala.concurrent.ExecutionContext.Implicits.global

class LevelDownloader {
  val levelStash = collection.mutable.Map[String, Level]()
  var levelList = List[String]()
  private var outstandingDownloads: Int = 0

  def getLevelByNum(num: Int): Option[Level] = {
    if (num >= levelList.length) None else levelStash.get(levelList(num))
  }

  def downloadWorld(url: String)(continuation: => Any) = {
    val (urlDir, fileName) = url.splitAt(url.lastIndexOf('/') + 1)
    for {
      response <- Fetch.fetch(url)
      text <- response.text()
    } {
      levelList = Level.levelNameListFromYAML(text)
      outstandingDownloads = levelList.length
      for ((levelName, levelNumber) <- levelList.zipWithIndex) {
        downloadLevelFile(levelName, urlDir + levelName + ".level", continuation)
      }
    }
  }

  def downloadLevelFile(id: String, url: String, continuation: => Any): Unit = {
    for {
      response <- Fetch.fetch(url)
      text <- response.text()
      level <- Level.levelFromYAML(text)
    } {
      levelStash(id) = level
      outstandingDownloads -= 1
      if (outstandingDownloads == 0) {
        continuation
      }
    }
  }
}
