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
import java.util.concurrent.atomic.AtomicInteger;

import com.hazelcast.core.TransactionalMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.hazelcast.util.HazelcastBenchmarkRunnable;
import org.yardstickframework.hazelcast.util.HazelcastBenchmarkUtils;

import static com.hazelcast.transaction.TransactionOptions.TransactionType.TWO_PHASE;
import static org.yardstickframework.BenchmarkUtils.println;

/**
 * Hazelcast benchmark that performs get TX operations.
 */
public class HazelcastGetTxBenchmark extends HazelcastAbstractBenchmark {
    /** Number of threads that populate the cache for query test. */
    private static final int POPULATE_THREAD_NUM = Runtime.getRuntime().availableProcessors() * 2;

    /** */
    public HazelcastGetTxBenchmark() {
        super("map");
    }

    /** {@inheritDoc} */
    @Override public void setUp(final BenchmarkConfiguration cfg) throws Exception {
        super.setUp(cfg);

        println(cfg, "Populating tx data...");

        long start = System.nanoTime();

        final AtomicInteger cnt = new AtomicInteger(0);

        HazelcastBenchmarkUtils.runMultiThreaded(new HazelcastBenchmarkRunnable() {
            @Override public void run(int threadIdx) throws Exception {
                for (int i = threadIdx; i < args.range() && !Thread.currentThread().isInterrupted();
                     i += POPULATE_THREAD_NUM) {
                    map.put(i, new SampleValue(i));

                    int keysPopulated = cnt.incrementAndGet();

                    if (keysPopulated % 100000 == 0)
                        println(cfg, "Populated keys: " + keysPopulated);
                }
            }
        }, POPULATE_THREAD_NUM, "populate-tx");

        println(cfg, "Finished populating tx data in " + ((System.nanoTime() - start) / 1_000_000) + "ms.");
    }

    /** {@inheritDoc} */
    @Override public boolean test(Map<Object, Object> ctx) throws Exception {
        int key = nextRandom(args.range());

        // Repeatable read isolation level is always used.
        TransactionOptions txOpts = new TransactionOptions().setTransactionType(TWO_PHASE);

        TransactionContext tCtx = hazelcast().newTransactionContext(txOpts);

        tCtx.beginTransaction();

        TransactionalMap<Object, Object> txMap = tCtx.getMap("map");

        try {
            Object val = txMap.get(key);
            tCtx.commitTransaction();
        }
        catch (Exception e) {
            println(cfg, "Yardstick transaction will be rollback.");

            e.printStackTrace(cfg.error());

            tCtx.rollbackTransaction();
        }

        return true;
    }
}
