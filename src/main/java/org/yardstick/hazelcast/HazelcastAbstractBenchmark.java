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

package org.yardstick.hazelcast;

import com.hazelcast.core.*;
import org.yardstick.*;
import org.yardstick.impl.util.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Abstract class for Hazelcast benchmarks.
 */
public abstract class HazelcastAbstractBenchmark extends BenchmarkDriverAdapter {
    /** Random number generator. */
    protected static final Random RAND = new Random();

    /** Cache name. */
    private final String cacheName;

    /** Arguments. */
    protected final HazelcastBenchmarkArguments args = new HazelcastBenchmarkArguments();

    /** Node. */
    private final HazelcastNode node = new HazelcastNode();

    /** Map. */
    protected IMap<Object, Object> map;

    /**
     * @param cacheName Cache name.
     */
    protected HazelcastAbstractBenchmark(String cacheName) {
        this.cacheName = cacheName;
    }

    /** {@inheritDoc} */
    @Override public void setUp(BenchmarkConfiguration cfg) throws Exception {
        super.setUp(cfg);

        BenchmarkUtils.jcommander(cfg.commandLineArguments(), args, "<hazelcast-driver>");

        node.start(cfg);

        map = node.hazelcast().getMap(cacheName);

        assert map != null;

        waitForNodes();
    }

    /** {@inheritDoc} */
    @Override public void tearDown() throws Exception {
        node.stop();
    }

    /** {@inheritDoc} */
    @Override public String description() {
        return args.parametersToString() + '_' + super.description();
    }

    /** {@inheritDoc} */
    @Override public String usage() {
        return BenchmarkUtils.usage(args);
    }

    /**
     * @return Grid.
     */
    protected HazelcastInstance hazelcast() {
        return node.hazelcast();
    }

    /**
     * @throws Exception If failed.
     */
    private void waitForNodes() throws Exception {
        final CountDownLatch nodesStartedLatch = new CountDownLatch(1);

        hazelcast().getCluster().addMembershipListener(new MembershipListener() {
            @Override public void memberAdded(MembershipEvent evt) {
                if (nodesStarted())
                    nodesStartedLatch.countDown();
            }

            @Override public void memberRemoved(MembershipEvent evt) {
                // No-op.
            }

            @Override public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
                // No-op.
            }
        });

        if (!nodesStarted()) {
            cfg.output().println("Waiting for " + (args.nodes() - 1) + " nodes to start...");

            nodesStartedLatch.await();
        }
    }

    /**
     * @return {@code True} if all nodes are started, {@code false} otherwise.
     */
    private boolean nodesStarted() {
        int rmtNodeCnt = args.clientMode() ? args.nodes() - 1 : args.nodes();

        return hazelcast().getCluster().getMembers().size() >= rmtNodeCnt;
    }

    /**
     * @param max Key range.
     * @return Next key.
     */
    protected int nextRandom(int max) {
        return RAND.nextInt(max);
    }

    /**
     * @param min Minimum key in range.
     * @param max Maximum key in range.
     * @return Next key.
     */
    protected int nextRandom(int min, int max) {
        return RAND.nextInt(max - min) + min;
    }
}
