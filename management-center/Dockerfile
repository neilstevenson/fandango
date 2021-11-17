#FROM hazelcast/management-center:latest-snapshot
#FROM hazelcast/management-center:4.2021.06
FROM hazelcast/management-center:5.0.2

# Args to pass to ENV, set by dockerfile-maven-plugin.
ARG LICENSE
ARG CLUSTER1_NAME
ARG CLUSTER1_ADDRESSLIST
ARG MY_ADMINUSER
ARG MY_ADMINPASSWORD

# To check health, uses port 8081 but internal URL. Preconfigure license, logon/password.
ENV JAVA_OPTS="-Dhazelcast.mc.healthCheck.enable=true \
 -Dhazelcast.mc.prometheusExporter.enabled=true \
 -Dhazelcast.mc.license=$LICENSE"

# Used by /mc-start.sh to create admin user
ENV MC_ADMIN_USER=$MY_ADMINUSER
ENV MC_ADMIN_PASSWORD=$MY_ADMINPASSWORD

# Preconfigure cluster connections
ENV MC_CLUSTER1_NAME=$CLUSTER1_NAME
ENV MC_CLUSTER1_ADDRESSLIST=$CLUSTER1_ADDRESSLIST

# Start Management Center
CMD ["bash", "-c", "set -euo pipefail \
      && echo @@@@@@@@@@@@@@@@@@@@ \
      && echo MC_ADMIN_USER: $MC_ADMIN_USER \
      && echo MC_ADMIN_PASSWORD: $MC_ADMIN_PASSWORD \
      && echo @@@@@@@@@@@@@@@@@@@@ \
      && echo bin/mc-conf.sh cluster add -H=${MC_DATA} --cluster-name=${MC_CLUSTER1_NAME} --member-addresses=${MC_CLUSTER1_ADDRESSLIST} \
      && bin/mc-conf.sh cluster add -H=${MC_DATA} --cluster-name=${MC_CLUSTER1_NAME} --member-addresses=${MC_CLUSTER1_ADDRESSLIST} \
      && echo @@@@@@@@@@@@@@@@@@@@ \
      && echo bin/mc-start.sh \
      && bin/mc-start.sh \
     "]
