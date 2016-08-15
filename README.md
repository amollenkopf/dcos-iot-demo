# dcos-iot-demo
<img src="images/00-overview/architecture.jpg"/>
This project demonstrates how to configure a full stack geo-enabled Internet of Things (IoT) solution using <a href="https://mesosphere.com/">Mesosphere's</a> open sourced <a href="https://dcos.io/">Data Center Operating System (DC/OS)</a> using <a href="https://www.docker.com/">Docker</a> containerization and <a href="http://mesos.apache.org/">Mesos</a> frameworks including <a href="https://mesosphere.github.io/marathon/">Marathon</a>, <a href="http://kafka.apache.org/">Kafka</a>, <a href="http://spark.apache.org/">Spark</a>, and <a href="http://elasticsearch.mesosframeworks.com/">Elasticsearch</a>.


## dcos-iot-demo in action:
<center><a href="https://youtu.be/tOPmPIHuV-o"><img src="images/00-overview/dcos-iot-demo-screenshot.jpg" height="75%" width="75%" ></a></center>

## create your own dcos-iot-demo environment:
1. <a href="docs/overview.md">Review the architecture & application overview</a><br>
2. <a href="docs/acs-setup.md">Get a DC/OS cluster running on the Microsoft Azure or Amazon Web Services</a><br>
3. <a href="docs/dcos-explore.md">Explore the DC/OS and Mesos dashboards</a><br>
4. <a href="docs/kafka-setup.md">Schedule Kafka brokers</a><br>
5. <a href="docs/es-setup.md">Schedule an Elasticsearch cluster</a><br>
6. <a href="docs/marathon-lb-setup.md">Schedule marathon-lb to run on public agents</a><br>
7. <a href="docs/azure-ports-setup.md">Open up Azure ports</a><br>
8. <a href="docs/map-setup.md">Schedule the map</a><br>
9. <a href="docs/app-setup.md">Schedule the components of the application</a><br>
10. <a href="docs/source-setup.md">Schedule a Source that emits events</a><br>

## other topics of interest:
1. <a href="docs/cleanup-demo.md">cleanup procedures between demo runs</a><br>
2. <a href="docs/running-local.md">running on a local machine for verification during development</a><br>
3. <a href="docs/running-dcos-old.md">running on DC/OS (old content)</a><br>
