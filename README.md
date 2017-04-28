<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# Yardstick Hazelcast Benchmarks
Yardstick Hazelcast is a set of <a href="http://hazelcast.org" target="_blank">Hazelcast Data Grid</a> benchmarks written on top of Yardstick framework.

## Yardstick Framework
Visit <a href="https://github.com/gridgain/yardstick" target="_blank">Yardstick Repository</a> for detailed information on how to run Yardstick benchmarks and how to generate graphs.

The documentation below describes configuration parameters in addition to standard Yardstick parameters.

## Installation
1. Create a local clone of Yardstick Hazelcast repository
2. Import Yardstick Hazelcast POM file into your project
3. Run `mvn package` command

## Provided Benchmarks
The following benchmarks are provided:

1. `HazelcastGetBenchmark` - benchmarks atomic distributed cache get operation
2. `HazelcastPutBenchmark` - benchmarks atomic distributed cache put operation
3. `HazelcastSetBenchmark` - benchmarks atomic distributed cache set operation
4. `HazelcastPutGetBenchmark` - benchmarks atomic distributed cache put and get operations together
5. `HazelcastSetGetBenchmark` - benchmarks atomic distributed cache set and get operations together
6. `HazelcastPutGetBatchBenchmark` - benchmarks atomic distributed cache set on many keys and getAll operations together
7. `HazelcastPutTxBenchmark` - benchmarks transactional distributed cache put operation
8. `HazelcastSetTxBenchmark` - benchmarks transactional distributed cache set operation
9. `HazelcastSetGetTxOptimisticBenchmark` - benchmarks transactional distributed cache set and get operations together
10. `HazelcastSetGetTxPessimisticBenchmark` - benchmarks transactional distributed cache set and getForUpdate operations together
11. `HazelcastSqlQueryBenchmark` - benchmarks distributed SQL query over cached data
12. `HazelcastSqlQueryPutBenchmark` - benchmarks distributed SQL query with simultaneous cache updates
13. `HazelcastSqlQuerySetBenchmark` - benchmarks distributed SQL query with simultaneous cache sets
14. `HazelcastPutAllBenchmark` - benchmarks atomic distributed cache putAll operation
15. `HazelcastSetAllTxBenchmark` - benchmarks transactional distributed cache set operations in one transaction
16. `HazelcastGetAllSetAllTxOptimisticBenchmark` - benchmarks transactional distributed cache set and get operations together
17. `HazelcastGetAllSetAllTxPessimisticBenchmark` - benchmarks transactional distributed cache set and getForUpdate operations together

## Writing Hazelcast Benchmarks
All benchmarks extend `HazelcastAbstractBenchmark` class. A new benchmark should also extend this abstract class and implement `test` method. This is the method that is actually benchmarked.

## Running Hazelcast Benchmarks
Before running Hazelcast benchmarks, run `mvn package` command. This command will compile the project and also will unpack scripts from `yardstick-resources.zip` file to `bin` directory.

### Properties And Command Line Arguments
> Note that this section only describes configuration parameters specific to Hazelcast benchmarks, and not for Yardstick framework. To run Hazelcast benchmarks and generate graphs, you will need to run them using Yardstick framework scripts in `bin` folder.

> Refer to [Yardstick Documentation](https://github.com/gridgain/yardstick) for common Yardstick properties and command line arguments for running Yardstick scripts.

The following Hazelcast benchmark properties can be defined in the benchmark configuration:

* `-nn <num>` or `--nodeNumber <num>` - Number of nodes (automatically set in `benchmark.properties`), used to wait for the specified number of nodes to start
* `-b <num>` or `--backups <num>` - Number of backups for every key
* `-hzcfg <path>` or `--hzConfig <path>` - Path to Hazelcast configuration file
* `-hzclicfg <path>` or `--hzClientConfig <path>` - Path to Hazelcast client configuration file
* `-sb` or `--syncBackups` - Flag indicating whether synchronous backups are used, asynchronous is a default
* `-nt` or `--nodeType` - Type of driver nodes. One of: `SERVER`, `CLIENT`, `LITE_MEMBER`
* `-r <num>` or `--range <num>` - Range of keys that are randomly generated for cache operations
* `-rb` or `--readBackups` - Flag indicating whether backup reads are enabled or not, disabled by default

For example if we need to run 2 `HazelcastNode` servers on localhost with `HazelcastPutBenchmark` benchmark on localhost, with number of backups set to 1, backups are synchronous, then the following configuration should be specified in `benchmark.properties` file:

```
SERVER_HOSTS=localhost,localhost

# Note that -dn and -sn, which stand for data node and server node, are
# native Yardstick parameters and are documented in Yardstick framework.
CONFIGS="-b 1 -sb -dn HazelcastPutBenchmark -sn HazelcastNode"
```

## Running on Amazon

This repo contains all necessary scripts and properties files for a comparison Hazelcast with other products.

For running on Amazon EC2 need to perform the following steps:

* Run Amazon EC2 instances. Choose number of instances and hardware according to your requirements.

The following actions need to perform on all instances:

* Install Java, Maven and Git on all instances.

```
For example for Ubuntu:

# apt-get install java
# apt-get install mvn
# apt-get install git
```

* Clone this repository and build project on one of the Amazon instances. Yardstick will copy all the built binaries on all nodes automatically.

```
git clone https://github.com/gridgain/yardstick-hazelcast

mvn clean package
```

* Change `SERVER_HOSTS` and `DRIVER_HOSTS` properties in `config/benchmark.properties` file.
`SERVER_HOSTS` is comma-separated list of IP addresses where servers should be started, one server per host.
`DRIVER_HOSTS` is comma-separated list of IP addresses where drivers should be started, one driver per host, if the
property is not defined then the driver will be run on localhost.
Property file contains many useful information about benchmarks such as `list of benchmarks`, `JVM opts` and etc. More details there
[Properties And Command Line Arguments](https://github.com/gridgain/yardstick#properties-and-command-line-arguments)

* Update IP addresses in network section from
`config/hazelcast-config.xml` and `config/hazelcast-client-config.xml` files. For example:

```
config/hazelcast-client-config.xml

...
  <network>
    <cluster-members>
      <address>XXX.XXX.XXX.1:57500</address>
      <address>XXX.XXX.XXX.2:57500</address>
      <address>XXX.XXX.XXX.3:57500</address>
    </cluster-members>
    <connection-timeout>10000</connection-timeout>
    <connection-attempt-limit>50</connection-attempt-limit>
  </network>
...
```

```
config/hazelcast-config.xml

...
  <network>
    <port auto-increment="true">57500</port>
    <join>
      <multicast enabled="false"/>
      <tcp-ip enabled="true">
        <member>XXX.XXX.XXX.1:57500</member>
        <member>XXX.XXX.XXX.2:57500</member>
        <member>XXX.XXX.XXX.3:57500</member>
      </tcp-ip>
    </join>
  </network>
...
```
* This repository contains two main set benchmark for sync and async backups (see more details about it [there](http://docs.hazelcast.org/docs/3.8/manual/html-single/index.html#backing-up-maps)).
By default used set of benchmark for async backups. If you want to run benchmarks for sync backup then need to use
`benchmark-sync.properties` file. Perform `./bin/benchmark-run-all.sh` script for async backups or `./bin/benchmark-run-all.sh ./config/benchmark-sync.properties` for sync backups.
For more details about running scripts see [Running Yardstick Benchmarks](https://github.com/gridgain/yardstick#running-yardstick-benchmarks).
* After execution the script in `result` folder will be saved to results of benchmarks. For visualisation of results can be used `bin/jfreechart-graph-gen.sh` script.
For more details about the script see [JFreeChart Graphs](https://github.com/gridgain/yardstick#jfreechart-graphs).

## Issues
Use GitHub [issues](https://github.com/gridgain/yardstick-hazelcast/issues) to file bugs.

## License
Yardstick Hazelcast is available under [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) Open Source license.
