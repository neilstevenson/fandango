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

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.config.KubernetesConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Common Hazelcast config for microservices, so they can
 * access data from Hazelcast cluster.
 * </p>
 */
@Configuration
@Slf4j
public class ApplicationConfig {

    @Value("${my.cluster0.name}")
    private String myCluster0Name;

    /**
     * <p>Configure for Kubernetes or direct connectivity.
     * </p>
     */
    @Bean
    public ClientConfig clientConfig() throws IOException {
        ClientConfig clientConfig = new XmlClientConfigBuilder("hazelcast-client.xml").build();

        ClientNetworkConfig clientNetworkConfig = clientConfig.getNetworkConfig();

        clientConfig.getSerializationConfig().addPortableFactory(1, new MyPortableFactory());

        if (System.getProperty("my.kubernetes.enabled", "").equals("true")) {
            KubernetesConfig kubernetesConfig = new KubernetesConfig();

            kubernetesConfig.setEnabled(true);
            kubernetesConfig.setProperty("service-dns",
                    System.getProperty("my.project") + "-"
                    + System.getProperty("my.site") + "-"
                    + "hazelcast.default.svc.cluster.local");

            clientNetworkConfig.setKubernetesConfig(kubernetesConfig);

            log.info("Kubernetes configuration: cluster:" + clientConfig.getClusterName()
                    + ": service-dns: "
                    + clientNetworkConfig.getKubernetesConfig().getProperty("service-dns"));
        } else {
            clientNetworkConfig.getKubernetesConfig().setEnabled(false);
            String cloudDiscoveryToken = clientNetworkConfig.getCloudConfig().getDiscoveryToken();
            log.info("Cloud enabled {} token {}", clientNetworkConfig.getCloudConfig().isEnabled(),
                    cloudDiscoveryToken);
            if (cloudDiscoveryToken != null && cloudDiscoveryToken.length() > 0) {
                clientNetworkConfig.getCloudConfig().setEnabled(true);
                /*FIXME Remove for PRD
                if (clientConfig.getClusterName().startsWith("de-")) {
                    clientConfig.setProperty("hazelcast.client.cloud.url", "https://dev.test.hazelcast.cloud");
                } else {
                    clientConfig.setProperty("hazelcast.client.cloud.url", "https://uat.hazelcast.cloud");
                }*/
                log.info("Cloud configuration: cluster: " + clientConfig.getClusterName()
                        + ": config: " + clientNetworkConfig.getCloudConfig());
            } else {
                clientConfig.setClusterName(this.myCluster0Name);
                clientNetworkConfig.getCloudConfig().setEnabled(false);
                String host = System.getProperty("HOST_IP", "127.0.0.1");
                if (host.indexOf(':') > 0) {
                    // Ignore port if provided, since we'll use several
                    host = host.substring(0, host.indexOf(":"));
                }
                int port = MyConstants.CLUSTER_BASE_PORT;

                List<String> memberList = List.of(host + ":" + port,
                        host + ":" + (port + 1), host + ":" + (port + 2));
                clientNetworkConfig.setAddresses(memberList);

                log.info("Non-Kubernetes configuration: cluster: "
                        + clientConfig.getClusterName() + ": member-list: "
                        + clientNetworkConfig.getAddresses());
            }
        }

        return clientConfig;
    }

}
