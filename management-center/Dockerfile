#FROM hazelcast/management-center:latest-snapshot
#FROM hazelcast/management-center:4.2021.06
FROM hazelcast/management-center:5.0.2

# Args to pass to ENV, set by dockerfile-maven-plugin.
ARG LICENSE
ARG CLUSTER0_NAME
ARG MY_ADMINUSER
ARG MY_ADMINPASSWORD

# To check health, uses port 8081 but internal URL. Preconfigure license, logon/password.
ENV JAVA_OPTS="-Dhazelcast.mc.healthCheck.enable=true \
 -Dhazelcast.mc.prometheusExporter.enabled=true \
 -Dhazelcast.mc.license=$LICENSE"

# Used by /mc-start.sh to create admin user
ENV MC_ADMIN_USER=$MY_ADMINUSER
ENV MC_ADMIN_PASSWORD=$MY_ADMINPASSWORD

# Preconfigure cluster connections. Cluster 1 is the cloud.
ENV MC_CLUSTER0_NAME=$CLUSTER0_NAME
ENV MC_CLUSTER0_PORT=6701
ENV CLUSTER0_ADDRESS_K8S=${CLUSTER0_NAME}-hazelcast.default.svc.cluster.local
# Turbine default
ENV MC_CLUSTER2_NAME=dev
ENV MC_CLUSTER2_PORT=5701
ENV CLUSTER2_ADDRESS_K8S=

# Start Management Center
CMD ["bash", "-c", "set -euo pipefail \
      && echo @@@@@@@@@@@@@@@@@@@@ \
      && echo MC_ADMIN_USER: $MC_ADMIN_USER \
      && echo MC_ADMIN_PASSWORD: $MC_ADMIN_PASSWORD \
      && echo @@@@@@@@@@@@@@@@@@@@ \
      && echo bin/mc-conf.sh cluster add -H=${MC_DATA} --cluster-name=${MC_CLUSTER0_NAME} \
             --member-addresses=${CLUSTER0_ADDRESS:-${CLUSTER0_ADDRESS_K8S}} \
      && bin/mc-conf.sh cluster add -H=${MC_DATA} --cluster-name=${MC_CLUSTER0_NAME} \
             --member-addresses=${CLUSTER0_ADDRESS:-${CLUSTER0_ADDRESS_K8S}} \
      && echo @@@@@@@@@@@@@@@@@@@@ \
      && if [[ ${CLUSTER2_ADDRESS:-${CLUSTER2_ADDRESS_K8S}} != '' ]] ; \
         then \
          echo bin/mc-conf.sh cluster add -H=${MC_DATA} --cluster-name=${MC_CLUSTER2_NAME} \
             --member-addresses=${CLUSTER2_ADDRESS:-${CLUSTER2_ADDRESS_K8S}} \
          && bin/mc-conf.sh cluster add -H=${MC_DATA} --cluster-name=${MC_CLUSTER2_NAME} \
             --member-addresses=${CLUSTER2_ADDRESS:-${CLUSTER2_ADDRESS_K8S}} \
          && echo @@@@@@@@@@@@@@@@@@@@ ; \
         fi \
      && echo bin/mc-start.sh \
      && bin/mc-start.sh \
     "]
