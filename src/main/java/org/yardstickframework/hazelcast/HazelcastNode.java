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
import com.hazelcast.client.config.*;
import com.hazelcast.config.*;
import com.hazelcast.core.*;
import org.yardstickframework.*;

import java.util.*;

import static org.yardstickframework.BenchmarkUtils.*;

/**
 * Standalone Hazelcast node.
 */
public class HazelcastNode implements BenchmarkServer {
    /** */
    private HazelcastInstance hz;

    /** Client mode. */
    private boolean clientMode;

    /** */
    public HazelcastNode() {
        // No-op.
    }

    /** */
    public HazelcastNode(boolean clientMode) {
        this.clientMode = clientMode;
    }

    /** */
    public HazelcastNode(boolean clientMode, HazelcastInstance hz) {
        this.clientMode = clientMode;
        this.hz = hz;
    }

    /** {@inheritDoc} */
    @Override public void start(BenchmarkConfiguration cfg) throws Exception {
        HazelcastBenchmarkArguments args = new HazelcastBenchmarkArguments();

        jcommander(cfg.commandLineArguments(), args, "<hazelcast-node>");

        // HazelcastNode can not run in client mode, except the case when it's used inside HazelcastAbstractBenchmark.
        if (clientMode) {
            ClientConfig clientCfg = new XmlClientConfigBuilder(args.clientConfiguration()).build();

            hz = HazelcastClient.newHazelcastClient(clientCfg);

            println(cfg, "Hazelcast client started.");
        }
        else {
            for (Map.Entry<String, String> env : System.getenv().entrySet())
                System.getProperties().setProperty(env.getKey(), env.getValue());

            Config hzCfg = new XmlConfigBuilder(args.configuration()).build();

            configure(args, hzCfg, "map", false);
            configure(args, hzCfg, "query", true);

            println(cfg, "Starting Hazelcast with configuration: " + hzCfg);

            hz = Hazelcast.newHazelcastInstance(hzCfg);

            println(cfg, "Hazelcast member started.");
            println(cfg, "Hazelcast benchmark arguments: " + args);
            println(cfg, "Hazelcast benchmark config: " + cfg);
        }

        assert hz != null;
    }

    /**
     * Configure Hazelcast map.
     *
     * @param args Arguments.
     * @param cfg Hazelcast config.
     * @param name Map name.
     * @param idx Flag to index or not.
     */
    private void configure(HazelcastBenchmarkArguments args, Config cfg, String name, boolean idx) {
        MapConfig mapCfg = cfg.getMapConfig(name);

        if (idx) {
            mapCfg.addMapIndexConfig(new MapIndexConfig("id", true));
            mapCfg.addMapIndexConfig(new MapIndexConfig("orgId", true));
            mapCfg.addMapIndexConfig(new MapIndexConfig("salary", true));
        }

        if (args.syncBackups()) {
            mapCfg.setBackupCount(args.backups());
            mapCfg.setAsyncBackupCount(0);
        }
        else {
            mapCfg.setBackupCount(0);
            mapCfg.setAsyncBackupCount(args.backups());
        }

        mapCfg.setReadBackupData(args.readBackups());
    }

    /** {@inheritDoc} */
    @Override public void stop() throws Exception {
        Hazelcast.shutdownAll();
        HazelcastClient.shutdownAll();
    }

    /** {@inheritDoc} */
    @Override public String usage() {
        return BenchmarkUtils.usage(new HazelcastBenchmarkArguments());
    }

    /**
     * @return Hazelcast instance.
     */
    public HazelcastInstance hazelcast() {
        return hz;
    }
}
