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
     * <p>User retrieval by primary key.
     * </p>
     */
    @GetMapping("/" + MyConstants.REST_CALL_USER + "/" + "{userId}")
    public CCUser user(@PathVariable String userId) {
        log.info("user('{}')", userId);

        IMap<String, CCUser> userMap
            = this.hazelcastInstance.getMap(MyConstants.IMAP_NAME_USER);

        CCUser ccUser = userMap.get(userId);

        log.debug("user() :: returning '{}'", ccUser);

        return ccUser;
    }

}
