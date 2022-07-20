/*
 * Copyright (c) 2008-2022, Hazelcast, Inc. All Rights Reserved.
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

import java.util.HashSet;
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

            int count = 0;
            while (this.hazelcastInstance.getLifecycleService().isRunning()) {
                TimeUnit.MINUTES.sleep(1);
                count++;
                String countStr = String.format("%05d", count);
                log.info("-=-=-=-=- {} '{}' {} -=-=-=-=-=-",
                        countStr, this.hazelcastInstance.getName(), countStr);
                if (count % FIVE == 0) {
                    this.logSizes();
                }
            }
        };
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

}
