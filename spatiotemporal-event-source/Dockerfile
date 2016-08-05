# maintainer: Adam Mollenkopf (@amollenkopf)
FROM centos
RUN yum install -y java-1.8.0-openjdk.x86_64
ADD ./data/taxi/taxi-simulation-01-25.csv /data/taxi/taxi-simulation-01-25.csv
ADD ./data/taxi/taxi-route.csv /data/taxi/taxi-route.csv
ADD ./data/bus/bus928.csv /data/bus/bus928.csv
ADD ./data/vehicle/vehicle412.csv /data/vehicle/vehicle412.csv
ADD ./target/scala-2.11/spatiotemporal-event-source-assembly-1.0.jar /jars/spatiotemporal-event-source-assembly-1.0.jar
