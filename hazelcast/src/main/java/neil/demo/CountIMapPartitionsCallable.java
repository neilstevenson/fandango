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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.map.IMap;
import com.hazelcast.partition.PartitionService;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Count the {@link com.hazelcast.map.IMap IMap} entry counts per partition.
 * </p>
 * <p>This gives an approximate count for the items in each partition.
 * It omits data in other structures such as {@link com.hazelcast.queue.IQueue}
 * as this demo doesn't feature them.
 * </p>
 * <p>It can give wrong results if invoked when a rebalance occurs.
 * </p>
 */
@Slf4j
public class CountIMapPartitionsCallable implements Callable<Map<Integer, Integer>>,
    HazelcastInstanceAware, Serializable {
    private static final long serialVersionUID = 1L;

    private transient HazelcastInstance hazelcastInstance;

    /**
     * <p><u>Watch this code may be long running.</u></p>
     * <p>For all maps, for all their local keys (ie. on this member)
     * increment the count for the key's partition.
     * </p>
     *
     * @return Map counts for partitions on this host.
     */
    @Override
    public Map<Integer, Integer> call() throws Exception {
        final Map<Integer, Integer> result = new HashMap<>();
        final PartitionService partitionService = this.hazelcastInstance.getPartitionService();

        try {
            this.hazelcastInstance.getDistributedObjects()
            .stream()
            .filter(distributedObject -> (distributedObject instanceof IMap))
            .filter(distributedObject -> !distributedObject.getName().startsWith("__"))
            .map(distributedObject -> ((IMap<?, ?>) distributedObject))
            .forEach(iMap -> {
                iMap.localKeySet()
                .forEach(key -> {
                    int partitionId = partitionService.getPartition(key).getPartitionId();
                    result.merge(partitionId, 1, Integer::sum);
                });
            });
        } catch (Exception e) {
            log.error("call()", e);
        }

        return result;
    }

    /**
     * <p>Called where the callable is run to plug in the
     * Hazelcast instance for the runner.
     * </p>s
     */
    @Override
    public void setHazelcastInstance(HazelcastInstance arg0) {
        this.hazelcastInstance = arg0;
    }

}
