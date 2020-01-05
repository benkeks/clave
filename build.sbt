enablePlugins(ScalaJSPlugin)

enablePlugins(WorkbenchPlugin)

//enablePlugins(ScalaJSBundlerPlugin)

name := "Clave"

version := "0.0.1"

scalaVersion := "2.13.1"


libraryDependencies ++= Seq(
  "org.scala-lang.modules" %%% "scala-collection-contrib" % "0.2.0",
  "org.scala-js" %%% "scalajs-dom" % "0.9.8"
)

jsDependencies ++= Seq(
  ProvidedJS / "lib/three.js" minified "lib/three.min.js" commonJSName "THREE",
  "org.webjars" % "jquery" % "3.4.1" / "jquery.js" minified "jquery.min.js",
  "org.webjars" % "bootstrap" % "4.4.1" / "bootstrap.js" minified "bootstrap.min.js" dependsOn "jquery.js"
)


scalacOptions ++= Seq("-deprecation")
