name := "spatiotemporal-event-source"

version := "1.0"

scalaVersion := "2.11.8"

val kafkaVersion = "0.9.0.1"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % kafkaVersion
)
