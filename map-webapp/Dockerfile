# maintainer: Adam Mollenkopf (@amollenkopf)
FROM centos
ADD ./target/universal/webapp-1.0.0.zip .
RUN yum install -y unzip
RUN yum install -y java-1.8.0-openjdk.x86_64
RUN yum install -y openssl
RUN unzip -o /webapp-1.0.0.zip -d /
RUN rm -rf /maven
