package org.denigma.threejs

import scalajs.js
import scalajs.js.annotation._

@js.native
@JSGlobal("THREE.GLTFLoader")
class GLTFLoader extends Loader {
  def load(url: String, onLoad: js.Function1[GLTF, Unit]): Unit = js.native
}

@js.native
class GLTF extends js.Object {
  def scene: Scene  = js.native
}