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

package org.yardstickframework.hazelcast.util;

import java.util.*;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.hazelcast.HazelcastBenchmarkArguments;

/**
 * Utils.
 */
public class HazelcastBenchmarkUtils {
    /**
     * Runs runnable in a give number of threads.
     *
     * @param r Runnable to execute.
     * @param threadNum Number of threads.
     * @param threadName Thread name pattern.
     * @return Execution errors if present, or empty collection in case of no errors.
     * @throws InterruptedException If execution was interrupted.
     */
    public static Collection<Throwable> runMultiThreaded(HazelcastBenchmarkRunnable r, int threadNum, String threadName)
        throws InterruptedException {
        List<HazelcastBenchmarkRunnable> runs = Collections.nCopies(threadNum, r);

        Collection<Thread> threads = new ArrayList<>();

        final Collection<Throwable> errors = Collections.synchronizedCollection(new ArrayList<Throwable>(threadNum));

        int threadIdx = 0;

        for (final HazelcastBenchmarkRunnable runnable : runs) {
            final int threadIdx0 = threadIdx;

            threads.add(new Thread(new Runnable() {
                @Override public void run() {
                    try {
                        runnable.run(threadIdx0);
                    }
                    catch (Exception e) {
                        e.printStackTrace();

                        errors.add(e);
                    }
                }
            }, threadName + threadIdx++));
        }

        for (Thread t : threads)
            t.start();

        // Wait threads finish their job.
        for (Thread t : threads)
            t.join();

        return errors;
    }

    /**
     *
     * @param cfg Benchmark configuration.
     */
    public static void setArgsFromProperties(BenchmarkConfiguration cfg, HazelcastBenchmarkArguments args){
        Map<String, String> customProps = cfg.customProperties();

        if(customProps.get("MAIN_CONFIG") != null)
            args.configuration(customProps.get("MAIN_CONFIG"));

        if(customProps.get("BACKUPS") != null)
            args.backups(Integer.valueOf(customProps.get("BACKUPS")));

        if(args.nodes() == 1){
            if (customProps.get("NODES_NUM") != null)
                args.nodes(Integer.valueOf(customProps.get("NODES_NUM")));
            else {
                int nodesNum = 0;

                String sHosts = customProps.get("SERVER_HOSTS");
                String dHosts = customProps.get("DRIVER_HOSTS");

                if (sHosts != null)
                    nodesNum += sHosts.split(",").length;

                if (dHosts != null)
                    nodesNum += dHosts.split(",").length;

                BenchmarkUtils.println(String.format("Setting nodes num as %d", nodesNum));

                args.nodes(nodesNum);
            }
        }
    }

    /**
     */
    private HazelcastBenchmarkUtils() {
        // No-op
    }
}
