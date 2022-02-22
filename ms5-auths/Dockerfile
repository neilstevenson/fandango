FROM library/openjdk:17-slim

# Maven compiled target
ARG JAR_FILE
COPY target/${JAR_FILE} application.jar

ENV JAVA_ARGS ""
ENV JAVA_OPTS ""

ENTRYPOINT exec java \
   $JAVA_ARGS $JAVA_OPTS \
   -DHOST_IP=$HOST_IP \
   -Dspring.zipkin.base-url=http://${HOST_IP}:9411/ \
   -jar application.jar    
