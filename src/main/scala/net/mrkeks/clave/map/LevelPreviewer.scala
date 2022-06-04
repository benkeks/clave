package net.mrkeks.clave.map

import org.scalajs.dom

class LevelPreviewer(context: dom.CanvasRenderingContext2D) {

  val tileColor = Map[MapData.Tile, List[Int]](
    MapData.Tile.Empty -> List(10,180,20,255),
    MapData.Tile.Crate -> List(200,200,160,255),
    MapData.Tile.SolidWall -> List(100,100,110,255)
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
