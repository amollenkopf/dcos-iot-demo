name := "spatiotemporal-esri-analytic-task"

version := "1.0"

scalaVersion := "2.10.5"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.6.1" % "provided",
  "org.apache.spark" %% "spark-streaming" % "1.6.1" % "provided",
  "org.apache.spark" %% "spark-streaming-kafka" % "1.6.1",
  "com.esri.geometry" % "esri-geometry-api" % "1.2.1"
)

assemblyExcludedJars in assembly := {
  val cp = (fullClasspath in assembly).value
  cp filter {
    x =>
      x.data.getName.matches("sbt.*") ||
      x.data.getName.matches(".*guava.*") ||
      x.data.getName.matches(".*yarn.*") ||
      x.data.getName.matches(".*unused.*") ||
      x.data.getName.matches(".*minlog.*") ||
      x.data.getName.matches(".*beanutils.*")
  }
}