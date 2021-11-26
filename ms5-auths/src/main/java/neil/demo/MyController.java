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
import java.util.Iterator;
import java.util.Objects;
import java.util.TreeSet;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastJsonValue;
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
     * <p>Return details for the provided input list of authorization ids.
     * </p>
     */
    @GetMapping("/" + MyConstants.REST_CALL_AUTHS + "/" + "{authIds}")
    public String[][] auths(@PathVariable String authIds) {
        log.info("auths('{}')", authIds);

        IMap<String, HazelcastJsonValue> authorisationMap
            = this.hazelcastInstance.getMap(MyConstants.IMAP_NAME_AUTHORIZATION);

        String[] auths = authIds.split(",");

        Collection<HazelcastJsonValue> values =
            authorisationMap.getAll(new TreeSet<>(Arrays.asList(auths))).values();
        String[][] result
            = new String[values.size()][];

        int i = 0;
        Iterator<HazelcastJsonValue> iterator = values.iterator();
        while (iterator.hasNext()) {
            JSONObject json = new JSONObject(iterator.next().toString());
            result[i] = new String[3];
            result[i][0] = Objects.toString(json.getDouble("amount"));
            result[i][1] = json.getString("authId");
            result[i][2] = json.getString("where");
            i++;
        }

        log.debug("auths() :: returning '{}' items for '{}' input", values.size(), auths.length);
        log.debug("auths() :: {}", Arrays.asList(result));

        return result;
    }

}
