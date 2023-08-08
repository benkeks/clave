enablePlugins(ScalaJSPlugin)

enablePlugins(JSDependenciesPlugin)

name := "Clave"

version := "0.4.1"

scalaVersion := "2.13.11"

scalacOptions ++= Seq("-deprecation")

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %%% "scala-collection-contrib" % "0.3.0",
  "org.scala-js" %%% "scalajs-dom" % "2.6.0",
  "io.crashbox" %%% "yamlesque" % "0.3.2"
)

jsDependencies ++= Seq(
  ProvidedJS / "lib/loaders/GLTFLoader.js" commonJSName "GLTFLoader"  dependsOn "facade_bundled/three.js",
)

Compile / fastOptJS / artifactPath :=
      ((Compile / classDirectory).value / "app" / ((fastOptJS / moduleName).value + ".js"))

Compile / fullOptJS / artifactPath := (Compile / fastOptJS / artifactPath).value

Compile / fullOptJS / scalaJSLinkerConfig ~= { _.withClosureCompiler(false) }

Global / excludeLintKeys += Compile / packageJSDependencies / artifactPath

Compile / packageJSDependencies / artifactPath := ((Compile / classDirectory).value / "app" / ((fastOptJS / moduleName).value + "-jsdeps.js"))