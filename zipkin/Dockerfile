FROM library/openjdk:17-slim

# Maven compiled target
ARG JAR_FILE
COPY target/${JAR_FILE} application.jar

ARG CLUSTER1_NAME

ENV JAVA_ARGS ""
ENV JAVA_OPTS "--add-modules java.se --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.management/sun.management=ALL-UNNAMED --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED"
ENV ZIPKIN_OPTS="-DSTORAGE_TYPE=hazelcast -Dzipkin.storage.hazelcast.cluster-name=$CLUSTER1_NAME"
#ENV ZIPKIN_OPTS=""

CMD ["bash", "-c", "set -euo pipefail \
      && echo @@@@@@@@@@@@@@@@@@@@ \
      && echo ZIPKIN_OPTS: $ZIPKIN_OPTS \
      && echo @@@@@@@@@@@@@@@@@@@@ \
      && exec java $JAVA_ARGS $JAVA_OPTS $ZIPKIN_OPTS -jar application.jar  \
     "]
   
