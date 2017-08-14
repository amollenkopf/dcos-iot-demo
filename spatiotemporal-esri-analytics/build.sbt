name := "spatiotemporal-esri-analytic-task"

version := "1.1"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.2.0" % "provided",
  "org.apache.spark" %% "spark-streaming" % "2.2.0" % "provided",
  "org.apache.spark" %% "spark-streaming-kafka-0-8" % "2.2.0",
  "org.elasticsearch" %% "elasticsearch-spark-20" % "5.5.1",
  "com.esri.geometry" % "esri-geometry-api" % "1.2.1",
  "org.apache.httpcomponents" % "httpclient" % "4.5.1"
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
        x.data.getName.matches(".*slf4j.*") ||
      x.data.getName.matches(".*beanutils.*")
  }
}
