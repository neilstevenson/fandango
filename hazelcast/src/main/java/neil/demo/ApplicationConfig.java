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

import com.hazelcast.config.ClasspathYamlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>Tweak the declarative config for non-Kubernetes environments.
 * </p>
 */
@Configuration
@Slf4j
public class ApplicationConfig {

    @Bean
    public Config config() {
        Config config = new ClasspathYamlConfig("hazelcast.yml");
        if (!System.getProperty("NODE_NAME", "").isBlank()) {
            config.setInstanceName(System.getProperty("NODE_NAME"));
        }

        NetworkConfig networkConfig = config.getNetworkConfig();

        if (System.getProperty("my.kubernetes.enabled", "").equals("true")) {
            log.info("Kubernetes configuration: service-dns: {}",
                    networkConfig.getJoin().getKubernetesConfig().getProperty("service-dns"));
        } else {
            networkConfig.getJoin().getKubernetesConfig().setEnabled(false);

            TcpIpConfig tcpIpConfig = new TcpIpConfig();
            tcpIpConfig.setEnabled(true);
            String host = System.getProperty("hazelcast.local.publicAddress", "127.0.0.1");
            if (host.indexOf(':') > 0) {
                // Ignore port if provided, since we'll use several
                host = host.substring(0, host.indexOf(":"));
            }
            int port1 = MyConstants.CLUSTER_BASE_PORT;
            int port2 = port1 + 1;
            int port3 = port1 + 2;
            tcpIpConfig.setMembers(List.of(host + ":" + port1, host + ":" + port2, host + ":" + port3));

            networkConfig.getJoin().setTcpIpConfig(tcpIpConfig);
            networkConfig.setPort(MyConstants.CLUSTER_BASE_PORT);

            log.info("Non-Kubernetes configuration: member-list: {}", tcpIpConfig.getMembers());
        }

        return config;
    }

}
