## Working on DC/OS:
### To configure a DC/OS:
<pre>
(1) TODO: Create a DC/OS cluster on Azure or Amazon
(2) TODO: Install packages on DC/OS:
    In DC/OS dashboard, go to 'Universe' and install the Marathon, Chronos, Kafka & Spark packages.
(3) TODO: Configure the DC/OS-CLI
    ~$ dcos config set core.dcos_url <your DC/OS url>
</pre>

### To build apps in preperation for installing them on DC/OS:
<pre>
(1) Configure docker:
    spatiotemporal-event-source$ docker-machine start default
    spatiotemporal-event-source$ eval "$(docker-machine env default)"
    spatiotemporal-event-source$ docker login
(2) Build & push event-source to DockerHub:
    spatiotemporal-event-source$ docker build -t amollenkopf/spatiotemporal-event-source .
    spatiotemporal-event-source$ docker push amollenkopf/spatiotemporal-event-source
(3) Build & upload spatiotemporal-esri-analytic-task to make it publicly available:
    spatiotemporal-esri-analytics$ sbt assembly
    Upload target/scala-2.10/spatiotemporal-esri-analytic-task-assembly-1.0.jar to S3/Azure Blob storage and make publicly available.
</pre>

### to run on DC/OS:
<pre>
(1) Configure Kafka topic:
    ~$ dcos kafka broker list
    ~$ dcos kafka topic list
    ~$ dcos kafka topic create source01 --partitions=3 --replication=1
    ~$ dcos kafka topic list
    ~$ dcos kafka topic describe source01
    ~$ dcos kafka topic create taxi-pickup --partitions=1 --replication=1
(2) Add event source as a Marathon app:
    event-source$ dcos marathon app add eventsource-docker.json
    note: you can verify events are being sent by running command line Kafka Consumer utilities to listen to the topic:
      azureuser@dcos-master-3F983CB-0:~$ wget http://mirror.reverse.net/pub/apache/kafka/0.9.0.1/kafka_2.10-0.9.0.1.tgz
      azureuser@dcos-master-3F983CB-0:~$ tar -xvf kafka_2.10-0.9.0.1.tgz
      azureuser@dcos-master-3F983CB-0:~$ cd kafka_2.10-0.9.0.1
      azureuser@dcos-master-3F983CB-0:~/kafka_2.10-0.9.0.1$ ./bin/kafka-console-consumer.sh --zookeeper master.mesos:2181/kafka --topic source01
(3) Add spatiotemporal-esri-analytics as a Marathon app:
    spatiotemporal-esri-analytics$ dcos marathon app add spatiotemporal-esri-analytic-task-docker.json
    to run w/ Spark directly:
    $ dcos spark run --submit-args="-Dspark.mesos.coarse=false
          --driver-cores 1 --driver-memory 1G --executor-cores 2 --executor-memory 1G
          --class org.cam.geo.analytics.esri.SpatiotemporalEsriAnalyticTask
          http://esri.box.com/s/w4rrhuxbh4bwitozcjhekqc4utszbmkb  
          broker-0.kafka.mesos:10040,broker-1.kafka.mesos:9312,broker-2.kafka.mesos:9601
          source01 source01-consumer-id false true"
    note: copy the driver-id, you will need it to kill the Spark app later
(4) Observe stdout of both

(5) Add spatiotemporal-store Elasticsearch cluster as a Marathon app:
    dcos-iot-demo-extras$ dcos marathon app add sat-marathon.json
(5) Install marathon-lb via Universe UI.
(6) Add map service as a Marathon app:
    dcos-iot-demo-extras$ dcos marathon app add badmf-marathon.json


(5) Remove apps:
    event-source$ dcos marathon app remove source01
    event-source$ dcos spark kill driver-20160503154055-0003
</pre>