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

import java.util.*;

import static org.yardstickframework.BenchmarkUtils.*;

/**
 * Hazelcast benchmark that performs put and get operations.
 */
public class HazelcastPutGetBenchmark extends HazelcastAbstractBenchmark {
    /** */
    public HazelcastPutGetBenchmark() {
        super("map");
    }

    /** {@inheritDoc} */
    @Override public boolean test(Map<Object, Object> ctx) throws Exception {
        int key = nextRandom(args.range());

        try {
            Object val = map.get(key);

            if (val != null)
                key = nextRandom(args.range());

            map.put(key, new SampleValue(key));
        }
        catch (Exception e){
            println("Failed put/get entry. Key [" + key + "].");

            e.printStackTrace();
        }

        return true;
    }
}
