enablePlugins(ScalaJSPlugin)

//enablePlugins(WorkbenchPlugin)

//enablePlugins(ScalaJSBundlerPlugin)

enablePlugins(JSDependenciesPlugin)

name := "Clave"

version := "0.2.0"

scalaVersion := "2.13.4"

//scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }

//workbenchDefaultRootObject := Some(("target/scala-2.13/classes/index.html", "target/scala-2.13/classes/"))

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %%% "scala-collection-contrib" % "0.3.0",
  "org.scala-js" %%% "scalajs-dom" % "2.1.0",
  "io.crashbox" %%% "yamlesque" % "0.3.0"
)

jsDependencies ++= Seq(
  // ProvidedJS / "lib/three.js" minified "lib/three.min.js" commonJSName "THREE",
  ProvidedJS / "lib/loaders/GLTFLoader.js" commonJSName "GLTFLoader"  dependsOn "facade_bundled/three.js",
  "org.webjars" % "jquery" % "3.4.1" / "jquery.js" minified "jquery.min.js",
  "org.webjars" % "bootstrap" % "4.4.1" / "bootstrap.js" minified "bootstrap.min.js" dependsOn "jquery.js"
)

//Compile / fastLinkJS / scalaJSLinkerOutputDirectory :=
//       (Compile / classDirectory).value / "app"

Compile / fastOptJS / artifactPath :=
      ((Compile / classDirectory).value / "app" / ((fastOptJS / moduleName).value + ".js"))

Compile / fullOptJS / artifactPath := (Compile / fullOptJS / artifactPath).value

Compile / packageJSDependencies /artifactPath := ((Compile / classDirectory).value / "app" / ((fastOptJS / moduleName).value + "-jsdeps.js"))

scalacOptions ++= Seq("-deprecation")
