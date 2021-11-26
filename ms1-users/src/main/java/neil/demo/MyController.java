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

import java.util.Collection;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.projection.Projection;
import com.hazelcast.projection.Projections;

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
     * <p>User ID and First Name projection.
     * </p>
     */
    @GetMapping("/" + MyConstants.REST_CALL_USERS)
    public Object[][] users() {
        log.info("users()");

        IMap<String, CCUser> userMap
            = this.hazelcastInstance.getMap(MyConstants.IMAP_NAME_USER);

        Projection<Entry<String, CCUser>, Object[]> projection
            = Projections.multiAttribute("userId", "firstName");

        Collection<Object[]> users = userMap.project(projection);
        log.debug("users() :: returning '{}' items", users.size());
        log.debug("users() :: {}", users);

        return users.toArray(new Object[users.size()][]);
    }

}
