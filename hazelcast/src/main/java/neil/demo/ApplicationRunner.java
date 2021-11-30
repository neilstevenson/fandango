/*
 * Copyright (c) 2008-2021, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neil.demo;

//import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
//import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
//import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Periodically log some information to the console.
 * </p>
 */
@Configuration
@Slf4j
public class ApplicationRunner {
    //private static final int FIVE = 5;

    @Value("${spring.application.name}")
    private String springApplicationName;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            log.info("${spring.applicationName}=='{}'", this.springApplicationName);
            log.info("-=-=-=-=- START '{}' START -=-=-=-=-=-",
                this.hazelcastInstance.getName());

            /* "ms-clients" don't use this cluster now, hence no need for data.
            for (String iMapName : MyConstants.IMAP_NAMES) {
                this.hazelcastInstance.getMap(iMapName);
            }

            IMap<String, CCUser> userMap
                = this.hazelcastInstance.getMap(MyConstants.IMAP_NAME_USER);
            if (!userMap.isEmpty()) {
                log.info("Skip loading, '{}' map not empty, assuming test data load already done",
                        userMap.getName());
            } else {
                this.loadTestData();
            }*/
            //this.logPartitions();

            /*
            int count = 0;
            while (this.hazelcastInstance.getLifecycleService().isRunning()) {
                TimeUnit.MINUTES.sleep(1);
                count++;
                String countStr = String.format("%05d", count);
                log.info("-=-=-=-=- {} '{}' {} -=-=-=-=-=-",
                        countStr, this.hazelcastInstance.getName(), countStr);
                if (count % FIVE == 0) {
                    //TODO this.logPartitions();
                    this.logSizes();
                    //TODO this.logJobs();
                }
            }
            */
        };
    }


    /**
     * <p>Insert test data
     * </p>
     *
    @SuppressWarnings("checkstyle:magicnumber")
    private void loadTestData() {
        IMap<String, HazelcastJsonValue> authorisationMap
            = this.hazelcastInstance.getMap(MyConstants.IMAP_NAME_AUTHORIZATION);
        IMap<String, CCTransaction> transactionMap
            = this.hazelcastInstance.getMap(MyConstants.IMAP_NAME_TRANSACTION);
        IMap<String, CCUser> userMap
            = this.hazelcastInstance.getMap(MyConstants.IMAP_NAME_USER);

        Arrays.stream(TestData.USERS).forEach((Object[] datum) -> {
            CCUser ccUser = new CCUser();

            ccUser.setUserId(datum[0].toString());
            ccUser.setFirstName(datum[1].toString());
            ccUser.setLastName(datum[2].toString());
            ccUser.setAuthIds(Arrays.asList(datum[3].toString().split(",")));
            ccUser.setTxnIds(Arrays.asList(datum[4].toString().split(",")));
            ccUser.setCreditLimit(Integer.parseInt(datum[5].toString()));

            userMap.set(ccUser.getUserId(), ccUser);
        });

        Arrays.stream(TestData.AUTHS).forEach((Object[] datum) -> {
            String authId = datum[0].toString();
            Double amount = Double.parseDouble(datum[1].toString());
            String where = datum[2].toString();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{");
            stringBuilder.append(" \"authId\" : \"" + authId + "\"");
            stringBuilder.append(",\"amount\" : " + amount);
            stringBuilder.append(",\"where\" : \"" + where + "\"");
            stringBuilder.append("}");
            HazelcastJsonValue ccAuthorisation = new HazelcastJsonValue(stringBuilder.toString());

            authorisationMap.set(authId, ccAuthorisation);
        });

        Arrays.stream(TestData.TXNS).forEach((Object[] datum) -> {
            CCTransaction ccTransaction = new CCTransaction();

            ccTransaction.setTxnId(datum[0].toString());
            ccTransaction.setAmount(Double.parseDouble(datum[1].toString()));
            ccTransaction.setWhere(datum[2].toString());
            ccTransaction.setWhen(Long.parseLong(datum[3].toString()));

            transactionMap.set(ccTransaction.getTxnId(), ccTransaction);
        });

        log.info("Loaded {} users, {} authorizations and {} transactions",
                userMap.size(), authorisationMap.size(), transactionMap.size()
                );

    }

    /**
     * <p>Assess the loading of the partitions.
     * </p>
     * <p>See <a href="https://hazelcast.com/blog/calculation-in-hazelcast-cloud/">here</a>
     * for a more efficient way to calculate Standard Deviation. Here we go for simplicity.
     * </p>
     *
    private void logPartitions() {
        CountIMapPartitionsCallable countIMapPartitionsCallable = new CountIMapPartitionsCallable();
        final Map<Integer, Tuple2<Integer, String>> collatedResults = new TreeMap<>();

        Map<Member, Future<Map<Integer, Integer>>> rawResults =
                this.hazelcastInstance.getExecutorService("default").submitToAllMembers(countIMapPartitionsCallable);

        rawResults.entrySet()
        .stream()
        .forEach(memberEntry -> {
            try {
                String member = memberEntry.getKey().getAddress().getHost() + ":" + memberEntry.getKey().getAddress().getPort();
                Map<Integer, Integer> result = memberEntry.getValue().get();
                result.entrySet().stream()
                .forEach(resultEntry -> collatedResults.put(resultEntry.getKey(), Tuple2.tuple2(resultEntry.getValue(), member)));
            } catch (Exception e) {
                log.error("logPartitions()", e);
            }
        });

        int partitionCountActual = collatedResults.size();
        int partitionCountExpected = this.hazelcastInstance.getPartitionService().getPartitions().size();

        // Less is ok, some may be empty.
        if (partitionCountActual > partitionCountExpected) {
            log.error("logPartitions() Results for {} partitions but expected {}", partitionCountActual, partitionCountExpected);
            return;
        }

        double total = 0d;
        final AtomicInteger max = new AtomicInteger(Integer.MIN_VALUE);
        final AtomicInteger min = new AtomicInteger(Integer.MAX_VALUE);
        for (int i = 0 ; i < partitionCountExpected; i++) {
            if (collatedResults.containsKey(i)) {
                int count = collatedResults.get(i).f0();
                total += count;
                if (count > max.get()) {
                    max.set(count);
                }
                if (count < min.get()) {
                    min.set(count);
                }
            }
        }
        double average = total / partitionCountActual;
        double stdDev = this.calculateStdDev(collatedResults, average);

        collatedResults.entrySet()
        .stream()
        .forEach(entry -> {
            int count = entry.getValue().f0();
            log.info("Partition {} - size {} - member {} {}{}",
                    String.format("%3d", entry.getKey()),
                    String.format("%7d", count),
                    String.format("%22s", entry.getValue().f1()),
                    (count == max.get() ? "MAXIMUM" : ""),
                    (count == min.get() ? "MINIMUM" : "")
                    );
        });
        log.info("Total {}, StdDev {}, Maximum {}, Mininum {}",
                Double.valueOf(total).intValue(), stdDev, max, min);
    }


    /**
     * <p>Calculate the deviation from the average.
     * </p>
     *
     * @param collatedResults
     * @param average
     * @return
     *
    private double calculateStdDev(Map<Integer, Tuple2<Integer, String>> collatedResults, double average) {
        double total = collatedResults.values()
        .stream()
        .mapToDouble(value -> {
            double diff = value.f0() - average;
            return diff * diff;
        })
        .sum();

        return Math.sqrt(total / collatedResults.size());
    }*/


    /**
     * <p>"{@code size()}" is a relatively expensive operation, but we
     * only run this every few minutes.
     * </p>
     * <p>This demo uses only {@link com.hazelcast.map.IMap IMap}.
     * </p>
     */
    private void logSizes() {
        Set<String> iMapNames = new TreeSet<>();
        Set<String> multiMapNames = new TreeSet<>();
        Map<String, Class<?>> otherNames = new TreeMap<>();

        for (DistributedObject distributedObject : this.hazelcastInstance.getDistributedObjects()) {
            if (!distributedObject.getName().startsWith("__")) {
                if (distributedObject instanceof IMap) {
                    iMapNames.add(distributedObject.getName());
                } else {
                    if (distributedObject instanceof MultiMap) {
                        multiMapNames.add(distributedObject.getName());
                    } else {
                        otherNames.put(distributedObject.getName(), distributedObject.getClass());
                    }
                }
            }
        }

        iMapNames
        .forEach(name -> {
            IMap<?, ?> iMap = this.hazelcastInstance.getMap(name);
            log.info("IMap '{}'.size() => {}", iMap.getName(), iMap.size());
            // HazelcastJsonValue isn't comparable
            if (name.startsWith("zipkin")) {
                new HashSet<>(iMap.keySet()).stream().forEach(key -> log.debug("    - key: '{}'", key));
            } else {
                new TreeSet<>(iMap.keySet()).stream().forEach(key -> log.debug("    - key: '{}'", key));
            }
        });
        multiMapNames
        .forEach(name -> {
            MultiMap<?, ?> multiMap = this.hazelcastInstance.getMultiMap(name);
            log.info("MultiMap '{}'.size() => {}", multiMap.getName(), multiMap.size());
            new TreeSet<>(multiMap.keySet()).stream().forEach(key -> log.debug("    - key: '{}'", key));
        });
        otherNames
        .entrySet()
        .forEach(entry -> {
            log.info("Distributed Object '{}' => {}", entry.getKey(), entry.getValue().getCanonicalName().toString());
        });

    }

    /**
     * <p>Jobs in name order.
     * </p>
     *
    private void logJobs() {
        try {
            Map<String, Job> jobs = new TreeMap<>();
            this.hazelcastInstance
                .getJet()
                .getJobs()
                .stream()
                .forEach(job -> jobs.put(job.getName(), job));

            jobs
            .forEach((key, value) -> {
                log.info("Job '{}' => {}", key, value.getStatus());
            });
        } catch (Exception e) {
            //log.error("logJobs()", e);
            log.error("logJobs() :: " + e.getMessage());
        }
    }*/
}
