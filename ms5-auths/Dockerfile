FROM library/openjdk:11-jre-slim

# Maven compiled target
ARG JAR_FILE
COPY target/${JAR_FILE} application.jar

ENV JAVA_ARGS ""
ENV JAVA_OPTS "--add-modules java.se --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.management/sun.management=ALL-UNNAMED --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED"

ENTRYPOINT exec java --illegal-access=deny \
   $JAVA_ARGS $JAVA_OPTS \
   -DHOST_IP=$HOST_IP \
   -Dspring.zipkin.base-url=${HOST_IP}:9411 \
   -jar application.jar    
