enablePlugins(ScalaJSPlugin)

enablePlugins(WorkbenchPlugin)

//enablePlugins(ScalaJSBundlerPlugin)

name := "Clave"

version := "0.1.2"

scalaVersion := "2.13.1"

workbenchDefaultRootObject := Some(("target/scala-2.13/classes/index.html", "target/scala-2.13/classes/"))

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %%% "scala-collection-contrib" % "0.2.0",
  "org.scala-js" %%% "scalajs-dom" % "0.9.8",
  "io.crashbox" %%% "yamlesque" % "0.2.0"
)

jsDependencies ++= Seq(
  ProvidedJS / "lib/three.js" minified "lib/three.min.js" commonJSName "THREE",
  ProvidedJS / "lib/loaders/GLTFLoader.js" commonJSName "GLTFLoader",
  "org.webjars" % "jquery" % "3.4.1" / "jquery.js" minified "jquery.min.js",
  "org.webjars" % "bootstrap" % "4.4.1" / "bootstrap.js" minified "bootstrap.min.js" dependsOn "jquery.js"
)

artifactPath in (Compile,fastOptJS) :=
      ((classDirectory in Compile).value / "app" / ((moduleName in fastOptJS).value + ".js"))

artifactPath in (Compile,fullOptJS) := (artifactPath in (Compile,fastOptJS)).value

Compile / packageJSDependencies / artifactPath := ((classDirectory in Compile).value / "app" / ((moduleName in fastOptJS).value + "-jsdeps.js"))

scalacOptions ++= Seq("-deprecation")
