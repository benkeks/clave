enablePlugins(ScalaJSPlugin)

enablePlugins(JSDependenciesPlugin)

name := "Clave"

version := "0.2.0"

scalaVersion := "2.13.10"

scalacOptions ++= Seq("-deprecation")

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %%% "scala-collection-contrib" % "0.3.0",
  "org.scala-js" %%% "scalajs-dom" % "2.1.0",
  "io.crashbox" %%% "yamlesque" % "0.3.0"
)

jsDependencies ++= Seq(
  ProvidedJS / "lib/loaders/GLTFLoader.js" commonJSName "GLTFLoader"  dependsOn "facade_bundled/three.js",
  "org.webjars" % "jquery" % "3.4.1" / "jquery.js" minified "jquery.min.js",
  "org.webjars" % "bootstrap" % "4.4.1" / "bootstrap.js" minified "bootstrap.min.js" dependsOn "jquery.js"
)

Compile / fastOptJS / artifactPath :=
      ((Compile / classDirectory).value / "app" / ((fastOptJS / moduleName).value + ".js"))

Compile / fullOptJS / artifactPath := (Compile / fastOptJS / artifactPath).value

Compile / fullOptJS / scalaJSLinkerConfig ~= { _.withClosureCompiler(false) }

Global / excludeLintKeys += Compile / packageJSDependencies / artifactPath

Compile / packageJSDependencies / artifactPath := ((Compile / classDirectory).value / "app" / ((fastOptJS / moduleName).value + "-jsdeps.js"))