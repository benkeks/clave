package net.mrkeks.clave.map

object Level {

  case class ObjectInfo(kind: String, x: Int, z: Int)

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
       objects = List())

  def parseCSV(csv: String): Array[Array[Int]] = {
    csv
      .split("\n")              // split by lines
      .map(_.split(","))        // split by columns
      .map(_.map(_.trim.toInt)) // convert to int
  }

  def levelFromYAML(txt: String): Option[Level] = {
    val yaml = yamlesque.read(txt).obj
    if (Set("name", "width", "height", "tilemap") subsetOf yaml.keySet) {
      val objects = for {
        yObj <- yaml.get("objects").toList
        node <- yObj.arr
        nObj = node.obj
      } yield Level.ObjectInfo(nObj("kind").str, nObj("x").num.toInt, nObj("z").num.toInt)
      Some(Level(yaml("name").str, yaml("width").num.toInt, yaml("height").num.toInt, parseCSV(yaml("tilemap").str), objects))
    } else {
      None
    }
  }

  def levelNameListFromYAML(txt: String): List[String] = {
    val yaml = yamlesque.read(txt).obj
    yaml("levels").arr.toList.map(_.str)
  }
}

case class Level(
  name: String,
  width: Int,
  height: Int,
  mapData: Array[Array[Int]],
  objects: List[Level.ObjectInfo]) {
}