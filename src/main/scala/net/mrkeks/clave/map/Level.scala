package net.mrkeks.clave.map

object Level {
  val level0 = Level(
      name = "Block 'em up",
      width = 16, height = 16,
      mapCsv =
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
       8, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0""")

  def levelFromYAML(txt: String): Option[Level] = {
    val yaml = yamlesque.read(txt).obj
    if (Set("name", "width", "height", "tilemap") subsetOf yaml.keySet) {
      Some(Level(yaml("name").str, yaml("width").num.toInt, yaml("height").num.toInt, yaml("tilemap").str))
    } else {
      None
    }
  }

  def levelNameListFromYAML(txt: String): List[String] = {
    val yaml = yamlesque.read(txt).obj
    yaml("levels").arr.toList.map(_.str)
  }
}

case class Level(name: String, width: Int, height: Int, mapCsv: String) {
}