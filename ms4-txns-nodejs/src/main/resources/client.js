'use strict';

const { Client } = require('hazelcast-client');
const fastify = require('fastify')();

// Injected by Maven
const cluster0_name = '@my.cluster0.name@'
const cluster1_name = '@my.cluster1.name@'
const cluster1_discovery_token = '@my.cluster1.discovery.token@'
const transaction_map_name = 'transaction'

// For use in Fastify
var transactionMap

class CCTransaction {
    constructor(txnId, amount, where, when) {
        this.txnId = txnId;
        this.amount = amount;
        this.where = where;
        this.when = when;
        this.factoryId = 1;
        this.classId = 2;
    }

    readPortable(reader) {
        this.txnId = reader.readString('txnId'); 
        this.amount = reader.readDouble('amount'); 
        this.where = reader.readString('where'); 
        this.when = reader.readLong('when'); 
    }

    writePortable(writer) {
        writer.writeString('txnId', this.txnId);
        writer.writeDouble('amount', this.amount);
        writer.writeString('where', this.where);
        writer.writeLong('when', this.when);
    }
}

function portableFactory(classId) {
    if (classId === 2) {
        return new CCTransaction();
    }
    return null;
}

function createClientConfig() {
    // Assume cloud
    var clusterName = cluster1_name;
    var network = {
        hazelcastCloud: {
            discoveryToken: cluster1_discovery_token
        },
        connectionTimeout: 5000
    }
    var properties = {
        'hazelcast.client.statistics.enabled': true
    }
    // Swap to local?
    if (cluster1_discovery_token === null || cluster1_discovery_token === "null" || cluster1_discovery_token.length == 0) {
        clusterName = cluster0_name
        network = {
            clusterMembers: [
                process.env.HOST_IP + ':6701'
            ],
            connectionTimeout: 5000
        }
        properties = {
            'hazelcast.client.statistics.enabled': true
        }
        console.log('createClientConfig(): Local:', clusterName, network, properties)
    } else {
        console.log('createClientConfig(): Cloud enabled:', clusterName, network, properties)
    }

    return {
        clusterName: clusterName,
        instanceName: '@project.artifactId@',
        clientLabels: [ '@build.timestamp@' ],
        network: network,
        connectionStrategy: {
            reconnectMode: 'off'
        },
        serialization: {
            portableFactories: {
                1: portableFactory
            }
        },
        properties: properties
    }
}

fastify.get('/turbine/config', (request, reply) => {
    var map = { 'serviceName': 'neil-ms4' }
    console.log('/turbine/config', map)
    return map
});

fastify.get('/txns/:txnIds', async (request, reply) => {
    var txnIds = request.params.txnIds
    console.log('/txn/', txnIds)
    var txns = txnIds.split(",")
    var entries = await transactionMap.getAll(txns)
    var result = [];
    for (let entry of entries) {
        var value = entry[1]
        var item = [ value.amount.toString(), value.txnId, value.when.toString(), value.where]
        result.push(item)
    }
    console.log('/txns', txnIds, ' => ', result)
    return result;
});

const start = async () => {
    try {
        var hazelcastClient = await Client.newHazelcastClient(createClientConfig());
        transactionMap = await hazelcastClient.getMap(transaction_map_name)
        await fastify.listen(8085)
    } catch (err) {
        console.error('Error:', err);
    }
}
start()
