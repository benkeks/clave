package net.mrkeks.clave.map

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
      Some(Level(
        name = yaml("name").str,
        width = yaml("width").str.toInt,
        height = yaml("height").str.toInt,
        mapData = parseCSV(yaml("tilemap").str),
        objects = objects,
        scorePerfect = yaml("score_perfect").str.toInt,
        scoreOkay = yaml("score_okay").str.toInt,
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
  scorePerfect: Int = 2,
  scoreOkay: Int = 1,
  version: Int = 0) {
}