/*
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.yardstickframework.hazelcast;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Hazelcast benchmark that performs put and get operations.
 */
public class HazelcastPutGetBatchBenchmark extends HazelcastAbstractBenchmark {
    /** */
    public HazelcastPutGetBatchBenchmark() {
        super("map");
    }

    /** {@inheritDoc} */
    @Override public boolean test(Map<Object, Object> ctx) throws Exception {
        Set<Object> keys = new TreeSet<>();

        while (keys.size() < args.batch())
            keys.add(nextRandom(args.range()));

        Map<Object, Object> vals = map.getAll(keys);

        Map<Integer, SampleValue> updates = new TreeMap<>();

        for (Object key : keys) {
            Object val = vals.get(key);

            if (val != null)
                key = nextRandom(args.range());

            updates.put((Integer)key, new SampleValue((Integer)key));
        }

        map.putAll(updates);

        return true;
    }
}
