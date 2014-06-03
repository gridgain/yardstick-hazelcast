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

1. `HazelcastPutBenchmark` - benchmarks atomic distributed cache put operation
2. `HazelcastPutGetBenchmark` - benchmarks atomic distributed cache put and get operations together
3. `HazelcastPutTxBenchmark` - benchmarks transactional distributed cache put operation
4. `HazelcastPutGetTxBenchmark` - benchmarks transactional distributed cache put and get operations together
5. `HazelcastSqlQueryBenchmark` - benchmarks distributed SQL query over cached data
6. `HazelcastSqlQueryPutBenchmark` - benchmarks distributed SQL query with simultaneous cache updates

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
* `-cm` or `--clientMode` - Flag indicating whether Hazelcast client is used
* `-r <num>` or `--range <num>` - Range of keys that are randomly generated for cache operations

For example if we need to run 2 `HazelcastNode` servers on localhost with `HazelcastPutBenchmark` benchmark on localhost, with number of backups set to 1, backups are synchronous, then the following configuration should be specified in `benchmark.properties` file:

```
HOSTS=localhost,localhost
    
# Note that -dn and -sn, which stand for data node and server node, are 
# native Yardstick parameters and are documented in Yardstick framework.
CONFIGS="-b 1 -sb -dn HazelcastPutBenchmark -sn HazelcastNode"
```

## Issues
Use GitHub [issues](https://github.com/gridgain/yardstick-hazelcast/issues) to file bugs.

## License
Yardstick Hazelcast is available under [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) Open Source license.
