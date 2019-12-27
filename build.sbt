enablePlugins(ScalaJSPlugin)

enablePlugins(WorkbenchPlugin)

name := "Clave"

version := "0.0.1"

scalaVersion := "2.12.8"

resolvers += sbt.Resolver.bintrayRepo("denigma", "denigma-releases") //for three js fascade

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  //"org.denigma" %%% "threejs-facade" % "0.0.74-0.1.7" // manually compiled /lib/
  "org.denigma" %%% "threejs-facade" % "0.0.77-0.1.8"
)

jsDependencies ++= Seq(
  "org.webjars" % "three.js" % "r77" / "three.min.js"
  //"org.webjars.npm" % "three-bmfont-text" % "2.3.0"
)

scalacOptions ++= Seq("-deprecation", "-Xmax-classfile-name", "140")
