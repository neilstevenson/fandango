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

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Defines application to Turbine
 * </p>
 */
@RestController
@Slf4j
public class TurbineController {

    @Value("${spring.application.name}")
    private String springApplicationName;

    @GetMapping("/" + MyConstants.TURBINE_REST_PREFIX + "/" + MyConstants.TURBINE_REST_CONFIG)
    public Map<String, String> config() {
        Map<String, String> map = Collections.singletonMap(MyConstants.TURBINE_CONFIG_SERVICE_NAME, this.springApplicationName);
        log.info("config() -> {}", map);
        return map;
    }

}
