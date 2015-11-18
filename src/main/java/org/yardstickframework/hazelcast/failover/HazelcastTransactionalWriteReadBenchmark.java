/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.yardstickframework.hazelcast.failover;

import com.hazelcast.core.TransactionalMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.hazelcast.transaction.TransactionOptions.TransactionType.TWO_PHASE;
import static org.yardstickframework.BenchmarkUtils.println;

/**
 *
 */
public class HazelcastTransactionalWriteReadBenchmark extends HazelcastFailoverAbstractBenchmark {
    /**
     *
     */
    public HazelcastTransactionalWriteReadBenchmark() {
        super("map");
    }

    /** {@inheritDoc} */
    @Override public boolean test(Map<Object, Object> ctx) throws Exception {
        final int k = nextRandom(args.range());

        assert args.keysCount() > 0 : "Count of keys: " + args.keysCount();

        final String[] keys = new String[args.keysCount()];

        for (int i = 0; i < keys.length; i++)
            keys[i] = "key-" + k + "-" + i;

        // Repeatable read isolation level is always used.
        TransactionOptions txOpts = new TransactionOptions().setTransactionType(TWO_PHASE)
            .setDurability(args.txDurability());

        TransactionContext tCtx = hazelcast().newTransactionContext(txOpts);

        tCtx.beginTransaction();

        TransactionalMap<String, Long> txMap = tCtx.getMap(cacheName);

        try {
            Map<String, Long> locMap = new LinkedHashMap<>();

            for (String key : keys) {
                Long val = txMap.getForUpdate(key);

                locMap.put(key, val);
            }

            Set<Long> values = new HashSet<>(locMap.values());

            if (values.size() != 1) {
                // Print all usefull information and finish.
                println(cfg, "Got different values for keys [map=" + locMap + "]");

                throw new IllegalStateException("Found different values for keys (see above information).");
            }

            final Long oldVal = locMap.get(keys[0]);

            final Long newVal = oldVal == null ? 0 : oldVal + 1;

            for (String key : keys)
                txMap.put(key, newVal);

            tCtx.commitTransaction();
        }
        catch (Exception e) {
            e.printStackTrace(cfg.error());

            tCtx.rollbackTransaction();
        }

        return true;
    }
}
