package net.mrkeks.clave.map

import org.scalajs.dom

class LevelPreviewer(context: dom.CanvasRenderingContext2D) {

  val tileColor = Map[MapData.Tile, List[Int]](
    MapData.Tile.Empty -> List(50,180,40,255),
    MapData.Tile.Crate -> List(200,200,160,255),
    MapData.Tile.SolidWall -> List(100,100,110,255),
    MapData.Tile.GateOpen -> List(140,140,140,255),
    MapData.Tile.Trigger -> List(30,40,250,255),
    MapData.Tile.TriggerWithCrate -> List(30,40,250,255),
    MapData.Tile.Monster -> List(10,100,20,255),
    MapData.Tile.Player -> List(230,10,20,255),
    MapData.Tile.Freezer -> List(180,210,250,255)
  ).withDefaultValue(List(255,255,255,255))

  protected def renderLevelThumbnail(level: Level): dom.ImageData  = {
    val image = context.createImageData(level.width, level.height)
    var idx = 0
    for (z <- 0 until level.height) {
      for (x <- 0 until level.width) {
        val tile = MapData.Tile(level.mapData(z)(x))
        val color = tileColor(tile)
        image.data(idx) = color(0)
        idx += 1
        image.data(idx) = color(1)
        idx += 1
        image.data(idx) = color(2)
        idx += 1
        image.data(idx) = color(3)
        idx += 1
      }
    }
    image
  }

  def getBase64(level: Level) = {
    val image = renderLevelThumbnail(level)
    context.canvas.width = image.width
    context.canvas.height = image.height
    context.putImageData(image, 0, 0)
    context.canvas.toDataURL("png")
  }
}
