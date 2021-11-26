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

import java.util.Map.Entry;

import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.serialization.GenericRecord;

/**
 * <p>Return a field, merely to prove serverside deserialization of JSON.
 * </p>
 * <p>Value is portable, {@link CCTransaction}.
 */
@SuppressWarnings({ "serial", "rawtypes" })
public class MyCCTransactionEntryProcessor implements EntryProcessor/*<String, Object, String>*/ {

    @Override
    public String process(Entry/*<String, Object>*/ entry) {
        Object value = entry.getValue();
        if (value instanceof GenericRecord) {
            GenericRecord genericRecord = (GenericRecord) value;
            return genericRecord.getFieldNames().toString();
        }
        return entry.getValue().getClass().getCanonicalName();
    }

}
