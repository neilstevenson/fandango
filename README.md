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
```
