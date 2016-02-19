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

import com.hazelcast.query.Predicates;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.hazelcast.querymodel.Person;

/**
 * Hazelcast benchmark that performs put and query operations.
 */
public class HazelcastCriteriaQuerySetBenchmark extends HazelcastAbstractBenchmark {

    /** */
    public HazelcastCriteriaQuerySetBenchmark() {
        super("query");
    }

    /** {@inheritDoc} */
    @Override public void setUp(final BenchmarkConfiguration cfg) throws Exception {
        super.setUp(cfg);
    }

    /** {@inheritDoc} */
    @Override public boolean test(Map<Object, Object> ctx) throws Exception {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        if (rnd.nextBoolean()) {
            double salary = rnd.nextDouble() * args.range() * 1000;

            double maxSalary = salary + 1000;

            Collection<Person> persons = executeQuery(salary, maxSalary);

            for (Person p : persons)
                if (p.getSalary() < salary || p.getSalary() > maxSalary)
                    throw new Exception("Invalid person retrieved [min=" + salary + ", max=" + maxSalary +
                        ", person=" + p + ']');
        }
        else {
            int i = rnd.nextInt(args.range());

            map.set(i, new Person(i, "firstName" + i, "lastName" + i, i * 1000));
        }

        return true;
    }

    /**
     * @param minSalary Min salary.
     * @param maxSalary Max salary.
     * @return Query results.
     * @throws Exception If failed.
     */
    @SuppressWarnings("unchecked")
    private Collection<Person> executeQuery(double minSalary, double maxSalary) throws Exception {
        return (Collection<Person>)(Collection<?>)map.values(Predicates.between("salary", minSalary, maxSalary));
    }
}
