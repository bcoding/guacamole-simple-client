FROM tomcat:8.5.37
MAINTAINER ab@bcoding.de

RUN cd / && \
    wget http://apache.lauf-forum.at/maven/maven-3/3.6.0/binaries/apache-maven-3.6.0-bin.tar.gz && \
    tar xfvz apache-maven-3.6.0-bin.tar.gz

RUN apt update -y && apt install openjdk-8-jdk -y

ADD . /app
WORKDIR /app
RUN /apache-maven-3.6.0/bin/mvn clean package war:exploded
RUN rm -r /usr/local/tomcat/webapps/*
RUN mv /app/target/guacamole-simple-client-1.0.0 /usr/local/tomcat/webapps/ROOT
RUN rm -r /app
WORKDIR /usr/local/tomcat