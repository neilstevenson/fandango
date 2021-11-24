FROM library/openjdk:17-slim

# Maven compiled target
ARG JAR_FILE
COPY target/${JAR_FILE} application.jar
COPY target/classes/hazelcast-client.yml /tmp

ARG CLUSTER1_NAME

ENV JAVA_ARGS ""
ENV JAVA_OPTS ""
#ENV ZIPKIN_OPTS="-DSTORAGE_TYPE=hazelcast -Dzipkin.storage.hazelcast.cluster-name=$CLUSTER1_NAME"
ENV ZIPKIN_OPTS="-DSTORAGE_TYPE=hazelcast -Dzipkin.storage.hazelcast.cluster-name=dev -Dzipkin.storage.hazelcast.client-config-file=/tmp/hazelcast-client.yml"
#ENV ZIPKIN_OPTS=""

CMD ["bash", "-c", "set -euo pipefail \
      && echo @@@@@@@@@@@@@@@@@@@@ \
      && echo ZIPKIN_OPTS: $ZIPKIN_OPTS \
      && echo @@@@@@@@@@@@@@@@@@@@ \
      && exec java $JAVA_ARGS $JAVA_OPTS $ZIPKIN_OPTS -jar application.jar  \
     "]
   
