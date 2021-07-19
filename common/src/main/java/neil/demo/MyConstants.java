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

import java.util.List;

import com.hazelcast.config.NetworkConfig;

/**
 * <p>Utility constants</p>
 */
public class MyConstants {

    //FIXME What to use in K8S ?
    public static final String TURBINE_CALL_PREFIX = "http://localhost:8466/v1/call";
    public static final String TURBINE_CONFIG_SERVICE_NAME = "serviceName";
    public static final String TURBINE_REST_PREFIX = "turbine";
    public static final String TURBINE_REST_CONFIG = "config";

    public static final String REST_CALL_USERS = "users";

    // Map names, for eager creation
    public static final String IMAP_NAME_USER  = "user";
    public static final List<String> IMAP_NAMES =
            List.of(IMAP_NAME_USER);

    // Must match Docker launch scripts
    public static final int CLUSTER_BASE_PORT = NetworkConfig.DEFAULT_PORT + 1000;
}
