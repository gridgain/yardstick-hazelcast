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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.hazelcast.querymodel.Account;
import org.yardstickframework.hazelcast.util.HazelcastBenchmarkRunnable;
import org.yardstickframework.hazelcast.util.HazelcastBenchmarkUtils;

import static com.hazelcast.transaction.TransactionOptions.TransactionType.TWO_PHASE;
import static org.yardstickframework.BenchmarkUtils.println;

/**
 * Hazelcast benchmark that performs transactional put and get operations.
 */
public class HazelcastAccountTxBenchmark extends HazelcastAbstractBenchmark {
    /** Number of threads that populate the cache for query test. */
    private static final int POPULATE_THREAD_NUM = Runtime.getRuntime().availableProcessors() * 2;

    /** */
    private static final int ACCOUNT_NUMBER = 3;

    /** */
    public HazelcastAccountTxBenchmark() {
        super("map");
    }

    /** {@inheritDoc} */
    @Override public void setUp(final BenchmarkConfiguration cfg) throws Exception {
        super.setUp(cfg);

        println(cfg, "Populating tx data...");

        long start = System.nanoTime();

        final AtomicInteger cnt = new AtomicInteger(0);

        // Populate persons.
        HazelcastBenchmarkUtils.runMultiThreaded(new HazelcastBenchmarkRunnable() {
            @Override public void run(int threadIdx) throws Exception {
                for (int i = threadIdx; i < args.range() && !Thread.currentThread().isInterrupted();
                    i += POPULATE_THREAD_NUM) {
                    map.put(i, new Account(100_000));

                    int populatedAccounts = cnt.incrementAndGet();

                    if (populatedAccounts % 100000 == 0)
                        println(cfg, "Populated accounts: " + populatedAccounts);
                }
            }
        }, POPULATE_THREAD_NUM, "populate-tx-account");

        println(cfg, "Finished populating tx data in " + ((System.nanoTime() - start) / 1_000_000) + "ms.");
    }

    /** {@inheritDoc} */
    @Override public boolean test(Map<Object, Object> ctx) throws Exception {
        Set<Integer> accountIds = new TreeSet<>();

        while (accountIds.size() < ACCOUNT_NUMBER)
            accountIds.add(nextRandom(args.range()));

        TransactionOptions txOpts = new TransactionOptions().setTransactionType(TWO_PHASE);

        TransactionContext tCtx = hazelcast().newTransactionContext(txOpts);

        tCtx.beginTransaction();

        TransactionalMap<Object, Object> txMap = tCtx.getMap("map");

        try {
            Map<Integer, Account> accounts = new HashMap<>();

            for (Integer id : accountIds) {
                // Lock the key (pessimistic tx)
                Account acc = (Account)txMap.getForUpdate(id);

                if (acc == null)
                    throw new Exception("Failed to find accounts: " + accountIds);

                accounts.put(id, acc);
            }

            Integer fromId = accountIds.iterator().next();

            int fromBalance = accounts.get(fromId).balance();

            for (Integer id : accountIds) {
                if (id.equals(fromId))
                    continue;

                Account account = accounts.get(id);

                if (fromBalance > 0) {
                    fromBalance--;

                    txMap.put(id, new Account(account.balance() + 1));
                }
            }

            txMap.put(fromId, new Account(fromBalance));

            tCtx.commitTransaction();
        }
        catch (Exception e) {
            e.printStackTrace(cfg.error());

            tCtx.rollbackTransaction();
        }

        return true;
    }
}
