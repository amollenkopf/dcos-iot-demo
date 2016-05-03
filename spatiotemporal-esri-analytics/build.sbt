name := "spatiotemporal-esri-analytic-task"

version := "1.0"

scalaVersion := "2.10.5"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.6.1" % "provided",
  "org.apache.spark" %% "spark-streaming" % "1.6.1" % "provided",
  "org.apache.spark" %% "spark-streaming-kafka" % "1.6.1",
  "org.elasticsearch" %% "elasticsearch-spark" % "2.3.0",
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