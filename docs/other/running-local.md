## Working locally (for verification prior to deploying on DC/OS):
It is useful to do development and verification locally prior to installing applications onto DC/OS.  This section will walk you through the process of setting up, building, and running individual apps locally prior to installing them onto DC/OS.
### To setup locally:
<pre>
(1) TODO: Setup SBT
(2) TODO: Setup Kafka
(3) TODO: Spark, go to Github, find the 1.6.1 release, download zip
    https://github.com/apache/spark/releases
    http://spark.apache.org/docs/latest/building-spark.html#building-for-scala-211
      $ ./dev/change-scala-version.sh 2.11
      $ mvn -Pyarn -Phadoop-2.4 -Dscala-2.11 -DskipTests clean package
      $ ./make-distribution.sh --name spark-1.6.1_2.11-bin-hadoop2.4 --tgz -Psparkr -Phadoop-2.4 -Phive -Phive-thriftserver -Pyarn
      
    ~$ export SPARK_HOME=~/spark-1.6.1-bin-hadoop2.6
</pre>

### To build locally:
<pre>
(1) Build event source:
    event-source$ sbt assembly
(2) Build Spark analytic tasks:
    spatiotemporal-esri-analytics$ sbt assembly
    TODO: add other analytics ...
</pre>

### To run locally:
<pre>
(1) Start Zookeeper:
    kafka_2.11-0.9.0.1$ ./bin/zookeeper-server-start.sh config/zookeeper.properties
(2) Start Kafka:
    kafka_2.11-0.9.0.1$ ./bin/kafka-server-start.sh config/server.properties
(3) Run Source:
    event-source$ java -jar target/scala-2.11/event-source-assembly-1.0.jar localhost:9092 source01 4 1000 true
    note: you can verify events are being sent by running a command line Kafka Consumer utility to listen to the topic:
    kafka_2.11-0.9.0.1$ ./bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic source01
(4) Run Spark analytic tasks:
    spatiotemporal-esri-analytics$ 
        $SPARK_HOME/bin/spark-submit --class "org.cam.geo.analytics.esri.SpatiotemporalEsriAnalyticTask"
            --master local[2] target/scala-2.10/spatiotemporal-esri-analytic-task-assembly-1.0.jar
            localhost:9092 source01 source01-consumer-id false true
        $SPARK_HOME/bin/spark-submit --class "org.cam.geo.analytics.esri.SpatiotemporalEsriAnalyticTaskWithElasticsearchSink"
            --master local[2] target/scala-2.10/spatiotemporal-esri-analytic-task-assembly-1.0.jar
            localhost:2181 source01 source01-consumer-id false true
            adammac.esri.com:9200 spatiotemporal-store
</pre>