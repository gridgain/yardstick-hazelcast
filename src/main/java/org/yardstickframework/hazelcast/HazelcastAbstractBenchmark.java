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

import com.hazelcast.client.*;
import com.hazelcast.config.CacheSimpleConfig;
import com.hazelcast.core.*;
import org.yardstickframework.*;

import java.util.*;
import java.util.concurrent.*;

import static org.yardstickframework.BenchmarkUtils.*;

/**
 * Abstract class for Hazelcast benchmarks.
 */
public abstract class HazelcastAbstractBenchmark extends BenchmarkDriverAdapter {
    /** Cache name. */
    protected final String cacheName;

    /** Arguments. */
    protected final HazelcastBenchmarkArguments args = new HazelcastBenchmarkArguments();

    /** Node. */
    private HazelcastNode node;

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

        jcommander(cfg.commandLineArguments(), args, "<hazelcast-driver>");

        HazelcastInstance instance = startedInstance(args.nodeType());

        if (instance == null) {
            node = new HazelcastNode(args.nodeType());

            node.start(cfg);
        }
        else
            node = new HazelcastNode(args.nodeType(), instance);

        waitForNodes();

        map = node.hazelcast().getMap(cacheName);

        assert map != null;
    }

    /**
     * @param nodeType Node type.
     * @return Started instance.
     */
    private static HazelcastInstance startedInstance(NodeType nodeType) {
        Collection<HazelcastInstance> col = nodeType == NodeType.CLIENT ? HazelcastClient.getAllHazelcastClients() :
            Hazelcast.getAllHazelcastInstances();

        return col == null || col.isEmpty() ? null : col.iterator().next();
    }

    /** {@inheritDoc} */
    @Override public void tearDown() throws Exception {
        map.clear();

        if (node != null)
            node.stop();
    }

    /** {@inheritDoc} */
    @Override public String description() {
        String desc = BenchmarkUtils.description(cfg, this);

        return desc.isEmpty() ?
            getClass().getSimpleName() + args.description() + cfg.defaultDescription() : desc;
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
            println(cfg, "Waiting for " + args.nodes() + " nodes to start...");

            nodesStartedLatch.await();
        }
    }

    /**
     * @return {@code True} if all nodes are started, {@code false} otherwise.
     */
    private boolean nodesStarted() {
        return numFullNodes() >= args.nodes();
    }

    /**
     * @return number of non-lite members in cluster.
     */
    private int numFullNodes() {
        int n = 0;

        for (Member node : hazelcast().getCluster().getMembers()) {
            if (!node.isLiteMember())
                n++;
        }

        return n;
    }

    /**
     * @param max Key range.
     * @return Next key.
     */
    protected int nextRandom(int max) {
        return ThreadLocalRandom.current().nextInt(max);
    }

    /**
     * @param min Minimum key in range.
     * @param max Maximum key in range.
     * @return Next key.
     */
    protected int nextRandom(int min, int max) {
        return ThreadLocalRandom.current().nextInt(max - min) + min;
    }
}
