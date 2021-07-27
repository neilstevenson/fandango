# fandango
Microservices that invoke each other

## Start

Run some Hazelcast servers, Zipkin and optionally Management Center

```
docker-hazelcast-0.sh
docker-hazelcast-1.sh
docker-hazelcast-2.sh
docker-management-center.sh
docker-zipkin.sh
```

Then

```
turbine-frontend.sh
turbine-ms1-users.sh
turbine-ms2-user.sh
turbine-ms3-balance-0.sh
turbine-ms3-balance-1.sh
turbine-ms3-balance-2.sh
turbine-ms4-txns.sh
turbine-ms5-auths.sh
```

You don't need three versions of microservice 3, it's just to show load balancing.
