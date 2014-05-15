## Yardstick Hazelcast
Yardstick Hazelcast is the set of Hazelcast benchmarks written by using Yardstick framework.
For more information about Yardstick framework, how to run it, build graphs and etc.
go to its own [repository](https://github.com/gridgain/yardstick).
For more information about Hazelcast In-Memory Data Grid visit [http://hazelcast.org](http://hazelcast.org).

## How to write your own Hazelcast benchmark
All benchmarks extend `HazelcastAbstractBenchmark` class. A new benchmark should also extend this
abstract class and implement `test` method. This is the method that is actually benchmarked.

## How to run Hazelcast benchmarks
Before the run the project should be compiled and the jar file is built. This is done by command `mvn package`.
Also this command unpacks benchmark scripts from `yardstick-resources.zip` file to `bin` directory.
The procedure of benchmarks run is the same as described in Yardstick
[documentation](https://github.com/gridgain/yardstick).

### Properties and command line arguments

The following Hazelcast benchmark properties can be defined in the benchmark configuration:

* `-nn <num>` or `nodeNumber <num>` - number of nodes, it is used by the benchmark driver to wait for the specified number of nodes are started,
    to not start the driver while not all nodes are ready
* `-b <num>` or `--backups <num>` - number of backups
* `-hzcfg <path>` or `--hzConfig <path>` - Hazelcast configuration file
* `-sb` or `--syncBackups` - flag indicating whether synchronous backups are used, asynchronous is default
* `-cm` or `--clientMode` - flag indicating whether Hazelcast client is used
* `-r <num>` or `--range <num>` - range of keys that are randomly generated for cache operations

## Maven Install
The easiest way to get started with Yardstick Hazelcast in your project is to use Maven dependency management:

```xml
<dependency>
    <groupId>org.yardstick.hazelcast</groupId>
    <artifactId>yardstick-hazelcast</artifactId>
    <version>${yardstick-hazelcast.version}</version>
</dependency>
```

You can copy and paste this snippet into your Maven POM file. Make sure to replace version with the one you need.

## Issues
Use GitHub [issues](https://github.com/gridgain/yardstick-hazelcast/issues) to file bugs.

## License
Yardstick Hazelcast is available under [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) license.
