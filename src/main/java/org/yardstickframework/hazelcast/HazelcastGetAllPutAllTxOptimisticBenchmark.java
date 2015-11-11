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

import com.hazelcast.core.TransactionalMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.hazelcast.transaction.TransactionOptions.TransactionType.TWO_PHASE;

/**
 * Hazelcast benchmark that performs getAll and putAll operations into optomistic
 * transaction (get and put's in for-loop).
 */
public class HazelcastGetAllPutAllTxOptimisticBenchmark extends HazelcastAbstractBenchmark {
    /** */
    public HazelcastGetAllPutAllTxOptimisticBenchmark() {
        super("map");
    }

    /** {@inheritDoc} */
    @Override public boolean test(Map<Object, Object> ctx) throws Exception {
        SortedSet<Integer> keys = new TreeSet<>();

        for (int i = 0; i < args.batch(); i++) {
            int key = nextRandom(args.range());

            keys.add(key);
        }

        // Repeatable read isolation level is always used.
        TransactionOptions txOpts = new TransactionOptions().setTransactionType(TWO_PHASE);

        TransactionContext tCtx = hazelcast().newTransactionContext(txOpts);

        tCtx.beginTransaction();

        TransactionalMap<Integer, Integer> txMap = tCtx.getMap("map");

        try {
            for (Integer key : keys)
                txMap.get(key);

            for (Integer key : keys)
                txMap.put(key, key);

            tCtx.commitTransaction();
        }
        catch (Exception e) {
            e.printStackTrace(cfg.error());

            tCtx.rollbackTransaction();
        }

        return true;
    }
}
