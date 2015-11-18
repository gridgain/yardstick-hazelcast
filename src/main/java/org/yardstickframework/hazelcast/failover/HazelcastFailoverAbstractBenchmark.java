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

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.hazelcast.HazelcastAbstractBenchmark;

import static org.yardstickframework.BenchmarkUtils.println;

/**
 */
public abstract class HazelcastFailoverAbstractBenchmark extends HazelcastAbstractBenchmark {
    /** */
    private static final AtomicBoolean restarterStarted = new AtomicBoolean();

    /** */
    private final AtomicBoolean firtsExProcessed = new AtomicBoolean();

    /**
     * @param cacheName Cache name.
     */
    protected HazelcastFailoverAbstractBenchmark(String cacheName) {
        super(cacheName);
    }

    /** {@inheritDoc} */
    @Override public void setUp(BenchmarkConfiguration cfg) throws Exception {
        super.setUp(cfg);

        // Wait for partitioned map exchange.
        Thread.sleep(10_000);
    }

    /** {@inheritDoc} */
    @Override public void onWarmupFinished() {
        if (cfg.memberId() == 0 && restarterStarted.compareAndSet(false, true)) {
            Thread restarterThread = new Thread(new Runnable() {
                @Override public void run() {
                    try {
                        println("Servers restarter started on driver: "
                            + HazelcastFailoverAbstractBenchmark.this.getClass().getSimpleName());

                        HazelcastInstance hz = hazelcast();

                        // Read servers configs from cache to local map.
                        IMap<Integer, BenchmarkConfiguration> srvsCfgsMap = hz.getMap("serversConfigs");

                        final Map<Integer, BenchmarkConfiguration> srvsCfgs = new HashMap<>();

                        for (Map.Entry<Integer, BenchmarkConfiguration> e : srvsCfgsMap.entrySet()) {
                            println("Read entry from 'serversConfigs' map : " + e);

                            srvsCfgs.put(e.getKey(), e.getValue());
                        }

                        final int backupsCnt = args.backups();

                        assert backupsCnt >= 1 : "Backups: " + backupsCnt;

                        final boolean isDebug = hz.getLoggingService().
                            getLogger(HazelcastFailoverAbstractBenchmark.class).isLoggable(Level.CONFIG);

                        // Main logic.
                        while (!Thread.currentThread().isInterrupted()) {
                            Thread.sleep(args.restartDelay() * 1000);

                            int numNodesToRestart = nextRandom(1, backupsCnt + 1);

                            List<Integer> ids = new ArrayList<>();

                            ids.addAll(srvsCfgs.keySet());

                            Collections.shuffle(ids);

                            println("Start servers restarting [numNodesToRestart=" + numNodesToRestart
                                + ", shuffledIds=" + ids + "]");

                            for (int i = 0; i < numNodesToRestart; i++) {
                                Integer id = ids.get(i);

                                BenchmarkConfiguration bc = srvsCfgs.get(id);

                                BenchmarkUtils.ProcessExecutionResult res = BenchmarkUtils.kill9Server(bc, isDebug);

                                println("Server with id " + id + " has been killed."
                                    + (isDebug ? " Process execution result:\n" + res : ""));
                            }

                            Thread.sleep(args.restartSleep() * 1000);

                            for (int i = 0; i < numNodesToRestart; i++) {
                                Integer id = ids.get(i);

                                BenchmarkConfiguration bc = srvsCfgs.get(id);

                                BenchmarkUtils.ProcessExecutionResult res = BenchmarkUtils.startServer(bc, isDebug);

                                println("Server with id " + id + " has been started."
                                    + (isDebug ? " Process execution result:\n" + res : ""));
                            }
                        }
                    }
                    catch (Throwable e) {
                        println("Got exception: " + e);
                        e.printStackTrace();

                        println(cfg, threadDump());

                        if (e instanceof Error)
                            throw (Error)e;
                    }
                }
            }, "servers-restarter");

            restarterThread.setDaemon(true);
            restarterThread.start();
        }

        Thread printerThread = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(30 * 60 * 1000);

                        println(cfg, threadDump());
                    }
                }
                catch (Throwable e) {
                    println("Got exception: " + e);
                    e.printStackTrace();

                    println(cfg, threadDump());

                    if (e instanceof Error)
                        throw (Error)e;
                }
            }
        }, "threads-printer");

        printerThread.setDaemon(true);
        printerThread.start();
    }


    /** {@inheritDoc} */
    @Override public void onException(Throwable e) {
        // Proceess only the first exception to prevent a multiple printing of a full thread dump.
        if (firtsExProcessed.compareAndSet(false, true)) {
            // Debug info on current client.
            e.printStackTrace(cfg.error());

            println("Full thread dump of the current node below.");

            println(cfg, threadDump());
        }
    }

    /**
     */
    public static String threadDump() {
        StringBuilder dump = new StringBuilder();

        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();

        ThreadInfo[] info = mxBean.getThreadInfo(mxBean.getAllThreadIds(), 500);

        for (ThreadInfo threadInfo : info) {
            dump.append('"');
            dump.append(threadInfo.getThreadName());
            dump.append("\" ");
            dump.append("\n   java.lang.Thread.State: ");
            dump.append(threadInfo.getThreadState());

            final StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();

            for (final StackTraceElement stackTraceElement : stackTraceElements) {
                dump.append("\n        at ");
                dump.append(stackTraceElement);
            }

            dump.append("\n\n");
        }

        return dump.toString();
    }
}
