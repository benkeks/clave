// Turn this project into a Scala.js project by importing these settings

import com.lihaoyi.workbench.Plugin._

enablePlugins(ScalaJSPlugin)

workbenchSettings

name := "Clave"

version := "0.0.1"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.8.0"
)

scalacOptions ++= Seq("-Xmax-classfile-name", "140")

bootSnippet := "net.mrkeks.clave.Clave().main();"

updateBrowsers <<= updateBrowsers.triggeredBy(fastOptJS in Compile)
