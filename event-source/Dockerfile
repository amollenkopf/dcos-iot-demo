# maintainer: Adam Mollenkopf (@amollenkopf)
FROM centos
RUN yum install -y java-1.8.0-openjdk.x86_64
ADD ./data/parolee/parolee.csv /data/parolee/parolee.csv
ADD ./data/people/people.csv /data/people/people.csv
ADD ./target/scala-2.11/event-source-assembly-1.0.jar /jars/event-source-assembly-1.0.jar
