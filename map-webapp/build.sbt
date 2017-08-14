import sbt.Keys._

name := "webapp"

val projectVersion = "1.0.0"

version := projectVersion

lazy val `webapp` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  cache,
  javaWs,
  ws,
  filters,
  specs2 % Test,
  "org.apache.httpcomponents" % "httpcore" % "4.4.5",
  "org.apache.httpcomponents" % "httpclient" % "4.5.2",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)