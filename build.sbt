// Turn this project into a Scala.js project by importing these settings

import com.lihaoyi.workbench.Plugin._

enablePlugins(ScalaJSPlugin)

workbenchSettings

name := "Clave"

version := "0.0.1"

scalaVersion := "2.11.8"

resolvers += sbt.Resolver.bintrayRepo("denigma", "denigma-releases") //for three js fascade

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  "org.denigma" %%% "threejs-facade" % "0.0.74-0.1.7"
)

scalacOptions ++= Seq("-Xmax-classfile-name", "140")

bootSnippet := "net.mrkeks.clave.Clave().main();"

updateBrowsers <<= updateBrowsers.triggeredBy(fastOptJS in Compile)
