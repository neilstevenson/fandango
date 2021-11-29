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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import com.hazelcast.sql.SqlResult;
import com.hazelcast.sql.SqlRow;
import com.hazelcast.sql.SqlRowMetadata;

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

            try {
                this.createOrReplaceMappings();
            } catch (Exception e) {
                log.error("createOrReplaceMappings()", e);
            }

            for (String iMapName : MyConstants.IMAP_NAMES) {
                this.hazelcastInstance.getMap(iMapName);
            }

            // Only one instance of this service, and data static, so safe to overwrite
            //IMap<String, CCUser> userMap
            //    = this.hazelcastInstance.getMap(MyConstants.IMAP_NAME_USER);
            //if (!userMap.isEmpty()) {
            //    log.info("Skip loading, '{}' map not empty, assuming test data load already done",
            //            userMap.getName());
            //} else {
                this.loadTestData();
                try {
                    this.runEntryProcessors();
                } catch (Exception e) {
                    log.error("runEntryProcessors()", e);
                }
            //}

            int count = 0;
            while (this.hazelcastInstance.getLifecycleService().isRunning()) {
                TimeUnit.MINUTES.sleep(1);
                String countStr = String.format("%05d", count);
                log.info("-=-=-=-=- {} '{}' {} -=-=-=-=-=-",
                        countStr, this.hazelcastInstance.getName(), countStr);
                if (count % FIVE == 0) {
                    this.logSizes();
                }
                count++;
            }
        };
    }


    /** Pre-JSON, {@code CCAuthorisation} was:
     * <pre>
     *  private String authId;
     *  private double amount;
     *  private String where;
     *  </pre>
     */
    private void createOrReplaceMappings() {
        this.hazelcastInstance.getSql().execute("SHOW MAPPINGS").forEach(row ->
            log.info("createOrReplaceMappings: BEFORE: " + row));
        String mapping1 = "CREATE OR REPLACE MAPPING \"" + MyConstants.IMAP_NAME_AUTHORIZATION + "\" "
                + " ("
                + "    __key VARCHAR,"
                + "    \"authId\" VARCHAR EXTERNAL NAME \"this.authId\","
                + "    \"amount\" DOUBLE EXTERNAL NAME \"this.amount\","
                + "    \"where\" VARCHAR EXTERNAL NAME \"this.where\""
                + ") "
                + "TYPE IMap "
                + " OPTIONS ( "
                + " 'keyFormat' = 'java',"
                + " 'keyJavaClass' = '" + String.class.getCanonicalName() + "',"
                + " 'valueFormat' = 'json-flat'"
                + " )";
        String mapping2 = "CREATE OR REPLACE MAPPING \"" + MyConstants.IMAP_NAME_TRANSACTION + "\" "
                + "TYPE IMap "
                + " OPTIONS ( "
                + " 'keyFormat' = 'java',"
                + " 'keyJavaClass' = '" + String.class.getCanonicalName() + "',"
                + " 'valueFormat' = 'java',"
                + " 'valueJavaClass' = '" + CCTransaction.class.getCanonicalName() + "'"
                + " )";
        String mapping3 = "CREATE OR REPLACE MAPPING \"" + MyConstants.IMAP_NAME_USER + "\" "
                + "TYPE IMap "
                + " OPTIONS ( "
                + " 'keyFormat' = 'java',"
                + " 'keyJavaClass' = '" + String.class.getCanonicalName() + "',"
                + " 'valueFormat' = 'java',"
                + " 'valueJavaClass' = '" + CCUser.class.getCanonicalName() + "'"
                + " )";
        for (String mapping : List.of(mapping1, mapping2, mapping3)) {
            try {
                log.info("Adding: " + mapping);
                this.hazelcastInstance.getSql().execute(mapping);
            } catch (Exception e) {
                log.error("createOrReplaceMappings():" + mapping, e);
            }
        }
        this.hazelcastInstance.getSql().execute("SHOW MAPPINGS").forEach(row ->
            log.info("createOrReplaceMappings:  AFTER: " + row));
    }


    /**
     * <p>Insert test data
     * </p>
     */
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
     * <p>Run entry processors to test serverside deserialization.
     * </p>
     */
    private void runEntryProcessors() {
        IMap<String, HazelcastJsonValue> authorisationMap
            = this.hazelcastInstance.getMap(MyConstants.IMAP_NAME_AUTHORIZATION);
        IMap<String, CCTransaction> transactionMap
            = this.hazelcastInstance.getMap(MyConstants.IMAP_NAME_TRANSACTION);

        MyCCAuthorisationEntryProcessor myCCAuthorisationEntryProcessor
            = new MyCCAuthorisationEntryProcessor();
        MyCCTransactionEntryProcessor myCCTransactionEntryProcessor
            = new MyCCTransactionEntryProcessor();

        for (String key : authorisationMap.keySet()) {
            Object result
                = authorisationMap.executeOnKey(key, myCCAuthorisationEntryProcessor);
            log.info("{} :EntryProcessor: key '{}' : {}", authorisationMap.getName(), key,
                    (result == null ? "<<null>>" : result.toString()));
        }
        for (String key : transactionMap.keySet()) {
            @SuppressWarnings("unchecked")
            Object result
                = transactionMap.executeOnKey(key, myCCTransactionEntryProcessor);
            log.info("{} :EntryProcessor: key '{}' : {}", transactionMap.getName(), key,
                    (result == null ? "<<null>>" : result.toString()));
        }
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
            // Use standard print if query fails
            if (!this.runSelectQuery(iMap.getName())) {
                // HazelcastJsonValue isn't comparable
                if (name.startsWith("zipkin")) {
                    new HashSet<>(iMap.keySet()).stream().forEach(key -> log.debug("    - key: '{}'", key));
                } else {
                    new TreeSet<>(iMap.keySet()).stream().forEach(key -> log.debug("    - key: '{}'", key));
                }
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
     * @param mapName
     * @return Any exception?
     */
    private boolean runSelectQuery(String mapName) {
        String sql = "SELECT * FROM \"" + mapName + "\"";
        log.debug("    " + sql);
        try {
            SqlResult sqlResult = this.hazelcastInstance.getSql().execute(sql);
            Iterator<SqlRow> iterator = sqlResult.iterator();
            int count = 0;
            while (iterator.hasNext()) {
                SqlRow sqlRow = iterator.next();
                log.debug("    " + count + ":");
                SqlRowMetadata sqlRowMetadata = sqlRow.getMetadata();
                for (int i = 0 ; i < sqlRowMetadata.getColumnCount(); i++) {
                    log.debug("     -:" + sqlRow.getObject(i));
                }
                count++;
            }
            log.debug("    [{} row{}] {}", count, (count == 1 ? "" : "s"), mapName);
            return true;
        } catch (Exception e) {
            log.error("runSelectQuery():" + sql + ":" + e.getMessage());
            return false;
        }
    }
}
