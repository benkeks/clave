package net.mrkeks.clave.map

import scalajs.js
import net.mrkeks.clave.view.DrawingContext
import org.denigma.threejs.MeshBasicMaterial
import org.denigma.threejs.BoxGeometry
import org.denigma.threejs.Geometry
import org.denigma.threejs.Mesh
import net.mrkeks.clave.game.GameObject
import org.denigma.threejs.Matrix4
import org.denigma.threejs.Vector3
import org.denigma.threejs.MeshFaceMaterial
import org.denigma.threejs.MeshLambertMaterial



class GameMap(val width: Int, val height: Int)
  extends GameObject with MapData {
  
  import MapData._
  
  private object Materials {
    val wall = new MeshLambertMaterial()
    wall.color.setHex(0xdddd99)
    
    val solidWall = new MeshLambertMaterial()
    solidWall.color.setHex(0x888899)
  }
  
  private val materials = new MeshFaceMaterial(js.Array(Materials.wall, Materials.solidWall))
  
  private val box = new BoxGeometry(1, 1, 1)
  box.faces.foreach { f => f.materialIndex = 0 }
  
  private val mesh = new Mesh(new Geometry(), materials)
  
  def init(context: DrawingContext) {
    context.scene.add(mesh)
  }
  
  def update(deltaTime: Double) {
    
  }
  
  def updateView() {
    val beginUpdate = js.Date.now
    
    val newGeometry = new Geometry()
    val drawingMatrix = new Matrix4()
    for (x <- 0 until width) {
      for (z <- 0 until height) {
        data(x)(z) match {
          case Tile.Wall =>
            drawingMatrix.makeTranslation(x, 0, z)
            newGeometry.merge(box, drawingMatrix.multiply(new Matrix4().makeRotationY(Math.random() * .1 - .05)), 0)
          case Tile.SolidWall =>
            drawingMatrix.makeTranslation(x, 0, z)
            newGeometry.merge(box, drawingMatrix, 1)
          case _ =>
        }
      }
    }
    mesh.geometry.dispose()
    mesh.geometry = newGeometry
    
    println("Map geometry update: " + (js.Date.now - beginUpdate) + "ms.")
  }
  
  def clear() {
    mesh.geometry.dispose()
  }
}