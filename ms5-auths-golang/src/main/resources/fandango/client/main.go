package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"strings"
	"time"

	"github.com/gorilla/mux"
	"github.com/hazelcast/hazelcast-go-client"
	"github.com/hazelcast/hazelcast-go-client/cluster"
	"github.com/hazelcast/hazelcast-go-client/serialization"
	"github.com/hazelcast/hazelcast-go-client/types"
)

// Injected by Maven
const cluster0_name = "@my.cluster0.name@"
const cluster1_name = "@my.cluster1.name@"
const cluster1_discovery_token = "@my.cluster1.discovery.token@"
const authorization_map_name = "authorization"

var authorization_map *hazelcast.Map
var ctx context.Context

type CCAuthorization struct {
	Amount float64 `json:"amount"`
	AuthId string  `json:"authId"`
	Where  string  `json:"where"`
}

func createClientConfig() hazelcast.Config {
	clientConfig := hazelcast.NewConfig()
	clientConfig.Cluster.ConnectionStrategy.ReconnectMode = cluster.ReconnectModeOff
	//TODO clientConfig.Cluster.ConnectionStrategy.Timeout = types.Duration(5 * time.Second)

	// Cloud or local?
	if len(cluster1_discovery_token) == 0 {
		clientConfig.Cluster.Name = cluster0_name
		clientConfig.Cluster.Cloud.Enabled = false
		host_ip := os.Getenv("HOST_IP")
		member := host_ip + ":6701"
		clientConfig.Cluster.Network.SetAddresses(member)
		log.Println("createClientConfig(): Local:", clientConfig.Cluster.Name, clientConfig.Cluster.Network.Addresses)
	} else {
                _ = os.Setenv("HZ_CLOUD_COORDINATOR_BASE_URL", "https://dev.test.hazelcast.cloud")
		clientConfig.Cluster.Name = cluster1_name
		clientConfig.Cluster.Cloud.Enabled = true
		clientConfig.Cluster.Cloud.Token = cluster1_discovery_token
		log.Println("createClientConfig(): Cloud enabled:", clientConfig.Cluster.Name, clientConfig.Cluster.Cloud)
	}

	clientConfig.ClientName = "@project.artifactId@"
	clientConfig.Cluster.Network.SSL.Enabled = false
	clientConfig.Stats.Enabled = true
	clientConfig.Stats.Period = types.Duration(time.Second)

	return clientConfig
}

func turbineConfig(w http.ResponseWriter, r *http.Request) {
	config := make(map[string]string)
	config["serviceName"] = "neil-ms5"
	log.Println("/turbine/config", config)
	json, err := json.Marshal(config)
	if err != nil {
		log.Fatal(err)
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	w.Write(json)
}

func auths(w http.ResponseWriter, r *http.Request) {
	authIds, err1 := mux.Vars(r)["authIds"]
	if err1 != true {
		log.Fatal("/auths/{authIds} missing '{authIds}'")
	}
	log.Println("/auths", authIds)
	auths := strings.Split(authIds, ",")
	var keys []interface{}
	for _, auth := range auths {
		keys = append(keys, auth)
	}
	entries, err2 := authorization_map.GetAll(ctx, keys...)
	if err2 != nil {
		log.Fatal(err2)
	}

	var result [][]string = make([][]string, 0)
	for _, entry := range entries {
		var value serialization.JSON = entry.Value.(serialization.JSON)
		var ccAuthorization CCAuthorization
		err3 := json.Unmarshal([]byte(value.String()), &ccAuthorization)
		if err3 != nil {
			log.Fatal(err3)
		}
		var resultLine []string = make([]string, 3)
		resultLine[0] = fmt.Sprintf("%.2f", ccAuthorization.Amount)
		resultLine[1] = ccAuthorization.AuthId
		resultLine[2] = ccAuthorization.Where
		result = append(result, resultLine)
	}

	log.Println("/auths", authIds, " => ", result)
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	json, _ := json.Marshal(result)
	w.Write(json)
}

func main() {
	ctx := context.Background()
	clientConfig := createClientConfig()
	hazelcastClient, err := hazelcast.StartNewClientWithConfig(ctx, clientConfig)
	if err != nil {
		log.Fatal(err)
		return
	}
	authorization_map, _ = hazelcastClient.GetMap(ctx, authorization_map_name)

	router := mux.NewRouter()
	router.HandleFunc("/turbine/config", turbineConfig)
	router.HandleFunc("/auths/{authIds}", auths)
	log.Fatal(http.ListenAndServe(":8086", router))
}
