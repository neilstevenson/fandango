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
import java.util.Collection;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Business logic for the microservice, one or more REST URLs.
 * </p>
 */
@RestController
@Slf4j
public class MyController {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    /**
     * <p>Return details for the provided input list of transaction ids.
     * </p>
     */
    @GetMapping("/" + MyConstants.REST_CALL_TXNS + "/" + "{txnIds}")
    public CCTransaction[] txns(@PathVariable String txnIds) {
        log.info("txns('{}')", txnIds);

        IMap<String, CCTransaction> transactionMap
            = this.hazelcastInstance.getMap(MyConstants.IMAP_NAME_TRANSACTION);

        String[] txns = txnIds.split(",");

        Collection<CCTransaction> values =
                transactionMap.getAll(new HashSet<>(Arrays.asList(txns))).values();

        log.debug("txns() :: returning '{}' items for '{}' input", values.size(), txns.length);
        log.debug("txns() :: {}", values.toArray(new CCTransaction[values.size()]).toString());

        return values.toArray(new CCTransaction[values.size()]);
    }

}
