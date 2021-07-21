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

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Job;
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
    private static final int FIVE = 5;

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
            }

            int count = 0;
            while (this.hazelcastInstance.getLifecycleService().isRunning()) {
                TimeUnit.MINUTES.sleep(1);
                count++;
                String countStr = String.format("%05d", count);
                log.info("-=-=-=-=- {} '{}' {} -=-=-=-=-=-",
                        countStr, this.hazelcastInstance.getName(), countStr);
                if (count % FIVE == 0) {
                    this.logSizes();
                    this.logJobs();
                }
            }
        };
    }


    /**
     * <p>Insert test data
     * </p>
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void loadTestData() {
        IMap<String, CCAuthorisation> authorisationMap
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
            CCAuthorisation ccAuthorisation = new CCAuthorisation();

            ccAuthorisation.setAuthId(datum[0].toString());
            ccAuthorisation.setAmount(Double.parseDouble(datum[1].toString()));
            ccAuthorisation.setWhere(datum[2].toString());

            authorisationMap.set(ccAuthorisation.getAuthId(), ccAuthorisation);
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
            new TreeSet<>(iMap.keySet()).stream().forEach(key -> log.debug("    - key: '{}'", key));
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
     */
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
    }
}