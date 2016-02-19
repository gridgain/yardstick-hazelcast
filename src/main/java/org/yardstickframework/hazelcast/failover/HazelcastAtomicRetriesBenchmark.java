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

import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import java.util.Map;

/**
 *
 */
public class HazelcastAtomicRetriesBenchmark extends HazelcastFailoverAbstractBenchmark {
    /**
     *
     */
    public HazelcastAtomicRetriesBenchmark() {
        super("map");
    }

    /** {@inheritDoc} */
    @Override public boolean test(Map<Object, Object> ctx) throws Exception {
        final int key = nextRandom(args.range());

        int opNum = nextRandom(4);

        switch (opNum) {
            case 0:
                map.get(key);

                break;

            case 1:
                map.set(key, String.valueOf(key));

                break;

            case 2:
                map.executeOnKey(key, new TestEntryProcessor());

                break;

            case 3:
                map.remove(key);

                break;

            default:
                throw new IllegalStateException("Got invalid operation number: " + opNum);
        }

        return true;
    }

    /**
     *
     */
    private static class TestEntryProcessor implements EntryProcessor {
        /** Serial version uid. */
        private static final long serialVersionUID = 0;

        /** {@inheritDoc} */
        @Override public Object process(Map.Entry entry) {
            return "key";
        }

        /** {@inheritDoc} */
        @Override public EntryBackupProcessor getBackupProcessor() {
            return new TestEntryBackupProcessor();
        }
    }

    /**
     *
     */
    private static class TestEntryBackupProcessor implements EntryBackupProcessor {
        /** Serial version uid. */
        private static final long serialVersionUID = 0;

        /** {@inheritDoc} */
        @Override public void processBackup(Map.Entry entry) {
            // No-op.
        }
    }
}
