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

import com.hazelcast.core.*;
import com.hazelcast.transaction.*;
import org.yardstickframework.*;
import org.yardstickframework.hazelcast.util.*;

import java.math.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import static com.hazelcast.transaction.TransactionOptions.TransactionType.*;
import static org.yardstickframework.BenchmarkUtils.*;

/**
 * Hazelcast benchmark that performs transactional batch put and get operations.
 */
public class HazelcastBatchPutGetTxBenchmark extends HazelcastAbstractBenchmark {
    /** */
    public static final int SHIFT = 1_000_000;

    /** Number of threads that populate the cache for query test. */
    private static final int POPULATE_QUERY_THREAD_NUM = Runtime.getRuntime().availableProcessors() * 2;

    /** */
    public static final String MAP_NAME = "map";

    /** */
    public HazelcastBatchPutGetTxBenchmark() {
        super(MAP_NAME);
    }

    /** {@inheritDoc} */
    @Override public void setUp(final BenchmarkConfiguration cfg) throws Exception {
        super.setUp(cfg);

        println(cfg, "Populating data...");

        long start = System.nanoTime();

        final AtomicInteger cnt = new AtomicInteger(0);

        // Populate persons.
        HazelcastBenchmarkUtils.runMultiThreaded(new HazelcastBenchmarkRunnable() {
            @Override
            public void run(int threadIdx) throws Exception {
                for (int i = threadIdx; i < args.range() && !Thread.currentThread().isInterrupted();
                    i += POPULATE_QUERY_THREAD_NUM) {
                    map.put(String.valueOf(SHIFT + i), BigDecimal.valueOf(i));

                    int populatedPersons = cnt.incrementAndGet();

                    if (populatedPersons % 100000 == 0)
                        println(cfg, "Populated : " + populatedPersons);
                }
            }
        }, POPULATE_QUERY_THREAD_NUM, "populate-data");

        println(cfg, "Finished populating data in " + ((System.nanoTime() - start) / 1_000_000) + "ms.");
    }

    /** {@inheritDoc} */
    @Override public boolean test(Map<Object, Object> ctx) throws Exception {
        Iterator<Map.Entry<String, BigDecimal>> changes = generateBatch().entrySet().iterator();

        TransactionOptions txOpts = new TransactionOptions().setTransactionType(TWO_PHASE);

        TransactionContext tCtx = hazelcast().newTransactionContext(txOpts);

        tCtx.beginTransaction();

        TransactionalMap<String, BigDecimal> txMap = tCtx.getMap(MAP_NAME);

        try {
            while (changes.hasNext()) {
                Map.Entry<String, BigDecimal> ent = changes.next();

                BigDecimal oldBalance = txMap.getForUpdate(ent.getKey());

                txMap.put(ent.getKey(), oldBalance.add(ent.getValue()));
            }

            tCtx.commitTransaction();
        }
        catch (Exception ex) {
            tCtx.rollbackTransaction();

            throw new Exception("transaction rolled back", ex);
        }

        return true;
    }

    /**
     * @return Batch.
     */
    private Map<String, BigDecimal> generateBatch() {
        SortedMap<String, BigDecimal> batch = new TreeMap<>();

        while (batch.size() < args.batchSize()) {
            String key = String.valueOf(nextRandom(SHIFT, SHIFT + args.range()));

            batch.put(key, BigDecimal.valueOf(nextRandom(1000)));
        }

        return batch;
    }
}
