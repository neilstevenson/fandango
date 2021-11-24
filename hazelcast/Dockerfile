# Use Python 3.7 as Grppcio:1.26.0 is incompatible with Python 3.8.
# Use Buster (Debian) as Alpine does not work well with wheel packages.
FROM library/python:3.7-buster

# Add Java
RUN apt update
RUN cd /tmp ; \
    wget -q https://download.java.net/java/GA/jdk17/0d483333a00540d886896bac774ff48b/35/GPL/openjdk-17_linux-x64_bin.tar.gz ; \
    tar xf openjdk-17_linux-x64_bin.tar.gz ; 
RUN update-alternatives --install /usr/bin/java java /tmp/jdk-17/bin/java 1
RUN java --version

# Download requirements into image makes start up faster when growing cluster
#COPY target/classes/requirements.txt /
#RUN pip3 install -r ./requirements.txt

# Maven compiled target
ARG JAR_FILE
COPY target/${JAR_FILE} application.jar

# Default values provided
ENV MY_KUBERNETES_ENABLED "true"

ENV JAVA_ARGS ""
ENV JAVA_OPTS ""

ENTRYPOINT exec java \
   $JAVA_ARGS $JAVA_OPTS \
   -Dmy.kubernetes.enabled=$MY_KUBERNETES_ENABLED \
   -DNODE_NAME=$NODE_NAME \
   -jar application.jar
