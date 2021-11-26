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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;

/**
 * <p>Entry point</p>
 * <p>But not Hazelcast for the FrontEnd</p>
 */
@SpringBootApplication(exclude = {
    HazelcastAutoConfiguration.class
    })
public class Application {

    //FIXME ms5
    //FIXME ms4

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

}
