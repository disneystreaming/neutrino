[![CI](https://img.shields.io/github/workflow/status/disneystreaming/neutrino/CI/main?label=CI&logo=github)](https://github.com/disneystreaming/neutrino/actions/workflows/ci.yml) [![license](https://img.shields.io/badge/license-TOST-informational)](https://disneystreaming.github.io/TOST-1.0.txt) [![CLA assistant](https://cla-assistant.io/readme/badge/disneystreaming/neutrino)](https://cla-assistant.io/disneystreaming/neutrino) [![release date](https://img.shields.io/github/release-date/disneystreaming/neutrino?logo=github)](https://github.com/disneystreaming/neutrino/releases)

# neutrino <!-- omit in toc -->

A dependency injection framework for apache spark with graceful serialization handling

<!-- START doctoc generated TOC please keep comment here to allow auto update -->

<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

- [What is the neutrino framework](#what-is-the-neutrino-framework)
- [Essential Information](#essential-information)
  - [Binary Releases](#binary-releases)
  - [How to build it](#how-to-build-it)
  - [About the license - TOST](#about-the-license---tost)
- [Why we need neutrino](#why-we-need-neutrino)
  - [The difficulty to apply DI on apache spark](#the-difficulty-to-apply-di-on-apache-spark)
  - [Start with a simple example](#start-with-a-simple-example)
- [How does the neutrino handle the serialization problem](#how-does-the-neutrino-handle-the-serialization-problem)
  - [Example code involved with neutrino](#example-code-involved-with-neutrino)
  - [How does neutrino work internally](#how-does-neutrino-work-internally)
    - [Full example code](#full-example-code)
    - [Constructor injection](#constructor-injection)
    - [Annotation binding](#annotation-binding)
    - [Applicable scenario](#applicable-scenario)
  - [How to transfer an instance with final type binding](#how-to-transfer-an-instance-with-final-type-binding)
    - [Low-level API: SerializableProvider](#low-level-api-serializableprovider)
    - [Annotation binding and constructor injection](#annotation-binding-and-constructor-injection)
    - [Applicable scenario](#applicable-scenario-1)
  - [Limitation](#limitation)
    - [Solution](#solution)
    - [Recommended solution](#recommended-solution)
  - [Private binding](#private-binding)
  - [Recover spark jobs from the checkpoint with neutrino](#recover-spark-jobs-from-the-checkpoint-with-neutrino)
- [Scopes](#scopes)
  - [Singleton per JVM scope](#singleton-per-jvm-scope)
  - [StreamingBatch scope](#streamingbatch-scope)
- [Other features](#other-features)
  - [Key spark objects are also injectable](#key-spark-objects-are-also-injectable)
  - [Injector Hierarchy / Child Injector](#injector-hierarchy--child-injector)
  - [Multiple dependency graphs in a single job](#multiple-dependency-graphs-in-a-single-job)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# What is the neutrino framework

The neutrino framework is a [Guice](https://github.com/google/guice) based dependency injection framework for apache spark and is designed to relieve the serialization work of development. More specifically, it will handle the serialization/deserialization work for the DI-generated objects automatically during the process of object transmission and checkpoint recovery.

The framework also provides some handy DI object scope management features, such as Singleton Scope per JVM, StreamingBatch scope (reuse the object in the same spark streaming batch per JVM).

In addition, the spark key utility objects such as SparkContext, SparkSession, StreamingContext are also injectable, which makes the spark job orchestration more flexible.

# Essential Information

## Binary Releases

You can add the dependency with maven like this:

```xml
<dependency>
    <groupId>com.disneystreaming.neutrino</groupId>
    <artifactId>neutrino-core_${scalaVersion}</artifactId>
    <version>${sparkVersion}_0.1.0</version>
</dependency>
```

for gradle

```groovy
compile "com.disneystreaming.neutrino:neutrino-core_${scalaVersion}:${sparkVersion}_0.1.0"
```

for sbt

```scala
libraryDependencies += "com.disneystreaming.neutrino" % "neutrino-core" % s"${sparkVersion}_0.1.0"
```

These are the current published versions:

<table align="center">
    <thead>
        <tr>
            <th scope="col">spark version</th>
            <th scope="col">scala version</th>
            <th scope="col">published binary</th>
        </tr>
    </thead>
    <tbody align="center">
        <tr>
            <td colspan="1" rowspan="1">2.0</td>
            <td>2.11</td>
            <td>
                <a href="https://search.maven.org/artifact/com.disneystreaming.neutrino/neutrino-core_2.11/2.0_0.1.0/jar">
                    <img src="https://img.shields.io/maven-central/v/com.disneystreaming.neutrino/neutrino-core_2.11/2.0_0.1.0.svg?label=Maven%20Central&style=flat&logo=appveyor" alt="Maven Central" />
                </a>
            </td>
        </tr>
        <tr>
            <td colspan="1" rowspan="1">2.1</td>
            <td>2.11</td>
            <td>
                <a href="https://search.maven.org/artifact/com.disneystreaming.neutrino/neutrino-core_2.11/2.1_0.1.0/jar">
                    <img src="https://img.shields.io/maven-central/v/com.disneystreaming.neutrino/neutrino-core_2.11/2.1_0.1.0.svg?label=Maven%20Central&style=flat&logo=appveyor" alt="Maven Central" />
                </a>
            </td>
        </tr>
        <tr>
            <td colspan="1" rowspan="1">2.2</td>
            <td>2.11</td>
            <td>
                <a href="https://search.maven.org/artifact/com.disneystreaming.neutrino/neutrino-core_2.11/2.2_0.1.0/jar">
                    <img src="https://img.shields.io/maven-central/v/com.disneystreaming.neutrino/neutrino-core_2.11/2.2_0.1.0.svg?label=Maven%20Central&style=flat&logo=appveyor" alt="Maven Central" />
                </a>
            </td>
        </tr>
        <tr>
            <td colspan="1" rowspan="1">2.3</td>
            <td>2.11</td>
            <td>
                <a href="https://search.maven.org/artifact/com.disneystreaming.neutrino/neutrino-core_2.11/2.3_0.1.0/jar">
                    <img src="https://img.shields.io/maven-central/v/com.disneystreaming.neutrino/neutrino-core_2.11/2.3_0.1.0.svg?label=Maven%20Central&style=flat&logo=appveyor" alt="Maven Central" />
                </a>
            </td>
        </tr>
        <tr>
            <td colspan="1" rowspan="2">2.4</td>
            <td>2.11</td>
            <td>
                <a href="https://search.maven.org/artifact/com.disneystreaming.neutrino/neutrino-core_2.11/2.4_0.1.0/jar">
                    <img src="https://img.shields.io/maven-central/v/com.disneystreaming.neutrino/neutrino-core_2.11/2.4_0.1.0.svg?label=Maven%20Central&style=flat&logo=appveyor" alt="Maven Central" />
                </a>
            </td>
        </tr>
        <tr>
            <td>2.12</td>
            <td>
                <a href="https://search.maven.org/artifact/com.disneystreaming.neutrino/neutrino-core_2.12/2.4_0.1.0/jar">
                    <img src="https://img.shields.io/maven-central/v/com.disneystreaming.neutrino/neutrino-core_2.12/2.4_0.1.0.svg?label=Maven%20Central&style=flat&logo=appveyor" alt="Maven Central" />
                </a>
            </td>
        </tr>
    </tbody>
</table>

## How to build it

We use JDK 8 and [gradle](https://gradle.org/) to build the project.

```shell
./gradlew clean build -Pscala-version=${scalaVersion} -Pspark-version=${sparkVersion}
```

The default value for `scalaVersion` is `2.11`, and the one for `sparkVersion` is `2.3`.

You can also add an option `-Pfast` to skip all the test cases and code style checks to make the build process faster.

## About the license - TOST

Disney's [Tomorrow Open Source Technology (TOST)](https://disneystreaming.github.io/TOST-1.0.txt) License is a Disney specific version of the Modified Apache 2.0. The main difference is referenced in the license to abide by Apache's license agreement - noted below.

> 6. Trademarks. This License does not grant permission to use the trade names, trademarks, service marks, or product names of the Licensor and its affiliates, except as required to comply with Section 4(c) of the License and to reproduce the content of the NOTICE file

# Why we need neutrino

## The difficulty to apply DI on apache spark

As we know, [dependency injection](https://en.wikipedia.org/wiki/Dependency_injection) (DI) is a famous design pattern that is widely used in Object-Oriented Programming (OOP). It separates the responsibility of "use" from the responsibility of "construction", and keeps modules evolving independently.

There are some mature dependency injection frameworks in the JVM world, such as [Guice](https://github.com/google/guice) and [Spring framework](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html), which are all designed to work properly in a single JVM process.

A spark job is a distributed application that requires the collaboration of multiple JVMs. The classic way to use DI in spark is to only apply it in the driver JVM. Under such circumstances, it is so common to pass some DI-generated functional objects<sup>*</sup> from the driver to executors, and spark requires the passed object and all its direct or in-direct dependencies to be serializable (as described by the following picture), which may need quite a lot of effort.

> *Note: Neutrino focuses on the functional objects (the ones containing processing logic) transition. The transition of data objects such as RDD elements have already been handled by spark very well

![serialize all dependencies](./images/deps_serialization.png)

You may think adding `java.io.Serializable` to the class definitions sounds boring but not too hard. But for some objects containing system resources (such as a redis connection) or classes defined in third party library, it is impossible for them to be serializable. Under such circumstances, usually a static field is created to hold the reference to those objects, which are hard to test and maintain.

## Start with a simple example

Consider a case where there is a user click behavior event stream, and we'd like to deduplicate the events in the stream, i.e. if the same user click the same item in the last 24 hours, the event should be filtered out.

Here we adopt [the Guice framework](https://github.com/google/guice) at the driver to build the spark application.

As shown in the code below, `EventFilter` interface abstacts the filtering logic, and is binded to subclass `RedisEventFilter`:

```scala
case class ClickEvent(userId: String, clickedItem: String)

trait EventFilter[T] {
    def filter(t: T): Boolean
}

class RedisEventFilter extends EventFilter[ClickEvent] { ... }

// Guice injector holds the dependency graph and instance can be generated from it
val eventFilter = injector.instance[EventFilter[ClickEvent]]
val eventStream: DStream[ClickEvent] = ...
eventStream.filter(e => eventFilter.filter(e))
```

The `eventFilter` instance has to be created on the driver and passed to executors because the DI graph only exists there. Then `RedisEventFilter` class should be serializable and can't depends on the redis client `JedisCommands` instance directly which is not serializable. But it has to be used in `RedisEventFilter` class at executors, so the possible way to do that is to hold it in a static field, like the code shown below:

```scala
object JedisCommandsHolder {
    val jedis = { /* create a JedisCommands instance from the config */ }
}

// `RedisEventFilter` should extend `Serializable` interface,
// since its instance needs to be passed to executors
class RedisEventFilter @Inject()()
extends EventFilter[ClickEvent] with Serializable {
   override def filter(e: ClickEvent): Boolean = {
       // There is a Lua script in redis, which checks if the item exists for the same user id.
       // if yes, the result `false` will be returned.
       // If no, the item will be saved under the user id with 24 hours as the TTL and the return value is `true`.
       JedisCommandsHolder.jedis.eval(DEDUP_SCRIPT,
                  Collections.singletonList(e.userId), 
                  Collections.singletonList(e.clickedItem))
   }
}
```

Then the class `RedisEventFilter` is hard to test since it references redis client in a static way and we can't replace it with a mock instance.

# How does the neutrino handle the serialization problem

## Example code involved with neutrino

With the neutrino framework, the class `RedisEventFilter` can be like this:

```scala
// The RedisEventFilter class depends on JedisCommands directly,
// and doesn't extend `java.io.Serializable` interface.
class RedisEventFilter @Inject()(jedis: JedisCommands)
extends EventFilter[ClickEvent] {
   override def filter(e: ClickEvent): Boolean = {
       jedis.eval(DEDUP_SCRIPT,
                  Collections.singletonList(e.userId), 
                  Collections.singletonList(e.clickedItem))
   }
}
```

And here is how to use it (nearly the same)

```scala
// the injector here is customized by neutrino and is similar to the Guice one.
val injector = ... 
val eventFilter = injector.instance[EventFilter[ClickEvent]]
val eventStream: DStream[ClickEvent] = ...
eventStream.filter(e => eventFilter.filter(e))
```

There are mainly 2 changes compared to the example code in the previous section:

- `RedisEventFilter` doesn't even have the `Serializable` interface.
- It depends on `JedisCommands` directly.

It is just like the code in a single JVM process. But how does it even work in the spark application?
It is because the `eventFilter` created by the neutrino injector is not the instance of class `RedisEventFilter` any more, and it is just a serializable stub/proxy class generated by neutrino, which is something like this:

```scala
// The `provider` can generate the actual instance from the graph in current JVM.
// the details will be discussed in the next section.
class EventFilterProxy[T] @Inject()(provider: Provider[T])
extends EventFilter[T] with Serialiable {
  override def filter(t: T): Boolean = {
      // request the actual instance and delegate the call on it
      provider.get().filter(t)
  }
}
```

The `provider` instance created by neutrino is serializable and can create the underlying implementation (`RedisEventFilter` class) with DI on the current JVM, even at the executors (Yeah, you are right. The neutrino have DI on both the driver and executors). And everytime the `filter` method is called, it delegate the call to the actual instance.

## How does neutrino work internally

As we know, to adopt the DI framework, we need to first build a dependency graph first, which describes the dependency relationship between various types. Guice uses Module API to build the graph while the Spring framework uses XML files or annotations.

The neutrino is built based on [Guice framework](https://github.com/google/guice), and of course, builds the dependency graph with the guice module API. It doesn't only keep the graph in the driver, but also has the same graph running on every executor, as shown in the picture below.

![serialize creation method](./images/serialize_creation_method.png)

In the dependency graph, some nodes may generate objects which may be passed to the executors, and neutrino framework would assign unique ids to these nodes. As every JVM have the same graph, the graph on each JVM have the same node id set.

If a DI-generated object is about to be passed to another JVM, instead of serializing the object itself and its dependencies, the neutrino framework encapsulates the creation method of the object (which contains the node id) in a small object (a.k.a the `provide`), passes the information to the target JVM, then find the corresponding node in the graph there and recreates it along with all dependencies with it. The object itself doesn't even need to be serializable.

To have neutrino generate the proxy, we just need to bind `EventFilter` to `RedisEventFilter` with a neutrino API. Here is the code to configure the bindings:

```scala
case class RedisConfig(host: String, port: Int)

// provider to generate the JedisCommands instance
// `redisConfig` can be read from config files
class RedisConnectionProvider @Inject()(redisConfig: RedisConfig)
extends Provider[JedisCommands] {
   override def get(): JedisCommands = {
       new Jedis(redisConfig.host, redisConfig.port)
   }
}

class FilterModule(redisConfig: RedisConfig) extends SparkModule {
   override def configure(): Unit = {
       bind[RedisConfig].toInstance(redisConfig)

       // Bind the provider of `JedisCommands` with `Singleton` scope
       // the `JedisCommands` will be kept singleton per JVM
       bind[JedisCommands].toProvider[RedisConnectionProvider].in[SingletonScope]

       // the magic is here
       // The method `withSerializableProxy` will generate a proxy 
       // extending `EventFilter` and `java.io.Serializable` interfaces with Scala macro.
       // The module must extend `SparkModule` or `SparkPrivateModule` to get it
       bind[EventFilter[ClickEvent]].withSerializableProxy
           .to[RedisEventFilter].in[SingletonScope]
   }
}
```

> Note: For details about how to bind types to their implementations, please refer to the [Guice](https://github.com/google/guice/wiki/GettingStarted) and [scala-guice](https://github.com/codingwell/scala-guice) doc.

The modules define the dependency relationship among the components. Because they need to be transferred to executors to create the same graph, all of them are required to be serializable. The neutrino framework provide an abstract base class `SparkModule` which extends `java.io.Serializable` and provides some utility methods.

Though the moduels still need to be serialized, it is way much easier than the object content serialization. For the `FilterModule` above, the only thing needs to be serialized is the `RedisConfig`.

And there is also another benefit. Before that a new object will be created every time it is passed to the target JVM (that is how java deserialization works), but since neutrino introduces a dependency graph in each JVM, the lifetime/scope of the passed objects in the executors can be managed by the graph out there. For example, we can specify a object's scope as `Singleton`, then the second time the object is passed to the same JVM, the object generated in the last time will be reused.

Actually, there is another way to run the filter logic on the executor:

```scala
eventStream
   .filter(e => injector.instance[EventFilter[ClickEvent]].filter(e))
```

This time the injector itself is passed to the executor and requests an `EventFilter` instance explicitly out there, i.e. the injector is serializable and will always reference the graph in current JVM.

Then how to create the injector? Because neutrino is based on Guice, its API is similar. And since Scala is the primary language in spark world, we also use its Scala extension ([scala-guice](https://github.com/codingwell/scala-guice)) in the API.

```scala
import com.disneystreaming.neutrino._

// injectorBuilder can be used to create the injector
val injectorBuilder = sparkSession.newInjectorBuilder()
val injector = injectorBuilder.newRootInjector(new FilterModule(redisConfig)) // multiple modules can be passed here
injectorBuilder.completeBuilding() // don't miss this call
```

The Guice uses modules to describe the dependency graph, and an injector containing the dependency graph can be created from them. The neutrino API is similar as Guice's.

But what's different from Guice is the `completeBuilding` calling, which seems redundant but is required. Because in the child/parent injector scenario, we need a call to mark the graph building completion (all necessary injectors are created), after which the graph is protected as readonly, then is serialized and sent to the executors. For single injector cases, the method `newSingleInjector` can be used without the `completeBuilding` calling.

### Full example code

Here is the full example code with neutrino:

```scala
import com.disneystreaming.neutrino._

// The RedisEventFilter class depends on JedisCommands directly,
// and doesn't extend `java.io.Serializable` interface.
class RedisEventFilter @Inject()(jedis: JedisCommands)
extends EventFilter[ClickEvent] {
   override def filter(e: ClickEvent): Boolean = {
       jedis.eval(DEDUP_SCRIPT,
                  Collections.singletonList(e.userId), 
                  Collections.singletonList(e.clickedItem))
   }
}

case class RedisConfig(host: String, port: Int)

// provider to generate the JedisCommands instance
// `redisConfig` can be read from config files
class RedisConnectionProvider @Inject()(redisConfig: RedisConfig)
extends Provider[JedisCommands] {
   override def get(): JedisCommands = {
       new Jedis(redisConfig.host, redisConfig.port)
   }
}

class FilterModule(redisConfig: RedisConfig) extends SparkModule {
   override def configure(): Unit = {
       bind[RedisConfig].toInstance(redisConfig)

       // Bind the provider of `JedisCommands` with `Singleton` scope
       // the `JedisCommands` will be kept singleton per JVM
       bind[JedisCommands].toProvider[RedisConnectionProvider].in[SingletonScope]

       // the magic is here
       // The method `withSerializableProxy` will generate a proxy 
       // extending `EventFilter` and `java.io.Serializable` interfaces with Scala macro.
       // The module must extend `SparkModule` or `SparkPrivateModule` to get it
       bind[EventFilter[ClickEvent]].withSerializableProxy
           .to[RedisEventFilter].in[SingletonScope]
   }
}

// injectorBuilder can be used to create the injector
val injectorBuilder = sparkSession.newInjectorBuilder()
val injector = injectorBuilder.newRootInjector(new FilterModule(redisConfig)) // multiple modules can be passed here
injectorBuilder.completeBuilding() // don't miss this call

val eventFilter = injector.instance[EventFilter[ClickEvent]]
val eventStream: DStream[ClickEvent] = ...
eventStream.filter(e => eventFilter.filter(e))
```

### Constructor injection

We can also inject the proxy to a class's constructor:

```scala
// injectable for constructors
class StreamHandler @Inject() (filter: EventFilter[ClickEvent]) {
    def handle(eventStream: DStream[ClickEvent]): Unit = {
        // assign it to a local variable to avoid serialization for the `StreamHandler` class
        val localFilter = filter
        eventStream
            .filter(e => localFilter.filter(e))
    }
}
```

### Annotation binding

And annotation binding is also supported:

```scala
bind[EventFilter[ClickEvent]].annotatedWith(Names.named("Hello"))
            .withSerializableProxy.to[RedisEventFilter].in[SingletonScope]
```

### Applicable scenario

Since we need to generate a subclass (proxy) of the binding interface, the binding type (which is `EventFilter` in this case) is required to be inheritable (interface or non-final class). And in the next section, we will introduce a low-level API to transfer final type instances.

## How to transfer an instance with final type binding

### Low-level API: SerializableProvider

The neutrino framework provides an interface `SerializableProvider` to handle final type binding case. This interface is nothing special:

```scala
trait SerializableProvider[T] extends Provider[T] with Serializable
```

For example, we have `EventProcessor` like this:

```scala
final class EventProcessor @Inject()() {
   def process(event: ClickEvent): Unit = {
       // processing logic
   }
}
```

And if we try to bind it with the same way introduced in the previous section, the compiler will report an error, because the proxy of a final class can't be generated by `withSerializableProxy`.

```scala
bind[EventProcessor].withSerializableProxy.to[EventProcessor].in[SingletonScope] // compiler error
```

The neutrino framework provides a API to bind the `SeriablzableProvider` of the class which encapsulates the logic to generate the instance from the graph.

```scala
class EventProcessorModule extends SparkModule {
   override def configure(): Unit = {
       // bind EventProcessor with singleton scope
       bind[EventProcessor].in[SingletonScope]

       // bind the SerializableProvider
       // The module must extend `SparkModule` or `SparkPrivateModule` to get it
       bindSerializableProvider[EventProcessor]
   }
}
```

The `SerializableProvider[EventProcessor]` can be used the same as the proxy discussed in the last section. It also holds the corresponding graph node id, and will create the `EventProcessor` from the graph in the current JVM. Actually, the proxy class in the last section is implemented based on the `SerializableProvider`.

```scala
val provider = injector.instance[SerializableProvider[EventProcessor]]

eventStream.map { e =>
   // call `get` method to request intance from the current gaph
   provider.get().process(e)
}
```

### Annotation binding and constructor injection

`SerializableProvider` also supports annotation binding:

- binding with annotation instances

```scala
bindSerializableProvider[EventProcessor](Names.named("Hello"))
```

- binding with annotation type

```scala
bindSerializableProvider[EventProcessor, TAnnotation]()
```

And the constructor injection is also supported.

### Applicable scenario

The `SerializableProvider` binding method can be used for both final and non-final type instance transition.

## Limitation

The general idea of the neutrino framework is to recreate the instances with the dependency graphs in the executors, which means any object state change in the driver can't be passed there with the generated proxy or `SerializableProvider`.

If you'd like to transfer some objects with mutable state, please bind it in the old way.

For example, if we change the `eventFilter` example a little bit, the dudup time interval is not fixed but is retrieved from a configuration store and might change over time. In this case, the `RedisEventFilter` may need some modification.

### Solution

One solution is to hold the interval as a mutable state in the `RedisEventFilter` class.

```scala
trait EventFilter[T] {
   def initialize(): Unit
   def filter(t: T): Boolean
}
 
class RedisEventFilter @Inject()(jedis: JedisCommands)
extends EventFilter[ClickEvent] with Serializable {
   private var durationMins = 24 * 60
 
   override def initialize(): Unit = {
       this.durationMins = // read the duration from the configuration store
   }
 
   override def filter(t: ClickEvent): Boolean = {
      jedis.eval(DEDUP_SCRIPT,
                Collections.singletonList(e.userId),
                Collections.singletonList(e.clickedItem, this.durationMins.toString))
   }
}
 
eventStream
   .foreachRDD { rdd =>
       val eventFilter = injector.instance[EventFilter[ClickEvent]]
       filter.initialize()
       rdd.filter(e => eventFilter.filter(e))
   }

```

A new mutable field `durationMins` has been added to store the actual cache duration, which is a mutable state and needs to be transfered to executors with the `RedisEventFilter` instance. So the `RedisEventFilter` has to implement `Serializable` interface this time.

Here is the binding definition:

```scala
class FilterModule(redisConfig: RedisConfig) extends SparkModule {
   override def configure(): Unit = {
       bind[RedisConfig].toInstance(redisConfig)
       bind[JedisCommands].withSerializableProxy
           .toProvider[RedisConnectionProvider].in[SingletonScope]
       bind[EventFilter[ClickEvent]].to[RedisEventFilter].in[SingletonScope]
   }
}
```

The `eventFilter` is binded and transfered like normal java objects. But it still depends on `JedisCommands`, so we need to generate a serializable proxy for `JedisCommands`. Then the `jedis` can delegate the redis call to the actual instance created by the graph there.

### Recommended solution

But I don't think it is a good design to use DI to generate some objects with mutable state. Retrieving duration from the config store can be seen as a pre-step to create the `RedisEventFilter` instance, so we can adopt `Abstract Factory` design pattern to solve this problem.

```scala
trait EventFilterFactory[T] {
   def getFilter(): EventFilter[T]
}
 
class RedisEventFilterFactory[T] @Inject()(jedis: JedisCommands) {
   def getFilter(): EventFilter[T] = {
       val durationMins = // read the duration from the configuration store
       new RedisEventFilter(jedis, durationMins)
   }
}
 
class RedisEventFilter @Inject()(jedis: JedisCommands, durationMins: Int)
extends EventFilter[ClickEvent] with Serializable {
   override def filter(t: ClickEvent): Boolean = {
      jedis.eval(DEDUP_SCRIPT,
                Collections.singletonList(e.userId),
                Collections.singletonList(e.clickedItem, this.durationMins.toString))
   }
}
 
class FilterModule(redisConfig: RedisConfig) extends SparkModule {
   override def configure(): Unit = {
       bind[RedisConfig].toInstance(redisConfig)
       bind[JedisCommands].withSerializableProxy
           .toProvider[RedisConnectionProvider].in[SingletonScope]
       bind[EventFilterFactory[ClickEvent]].to[RedisEventFilterFactory[ClickEvent]]
           .in[SingletonScope]
   }
}
 
eventStream
   .foreachRDD { rdd =>
       val eventFilter = injector.instance[EventFilterFactory[ClickEvent]].createFilter()
       rdd.filter(e => eventFilter.filter(e))
   }
```

## Private binding

As we know, Guice has a functionality called private binding which hides configuration and exposes only necessary bindings. The neutrino framework also support it.

- For inheritable type binding

```scala
class FilterModule(redisConfig: RedisConfig) extends SparkPrivateModule {
   override def configure(): Unit = {
       // other bindings
       ...

       bind[EventFilter[ClickEvent]].withSerializableProxy
           .to[RedisEventFilter].in[SingletonScope]
       // expose the interface which is actually the proxy
       expose[EventFilter[ClickEvent]]
   }
}
```

- For final type binding

```scala
class EventProcessorModule extends SparkPrivateModule {
   override def configure(): Unit = {
       bind[EventProcessor].in[SingletonScope]
       bindSerializableProvider[EventProcessor]
       expose[SerializableProvider[EventProcessor]]
   }
}
```

The one with annotations has the similar API. For details about that, please refer to [scala-guice](https://github.com/codingwell/scala-guice)'s doc.

## Recover spark jobs from the checkpoint with neutrino

Sometimes we need to enable the checkpoint in case of a job failure, which requires some closure objects used in RDD DAG creation to be serializable, even they are only used in the driver.

With neutrino, this problem can also be handled gracefully. It can recover the injectors and all objects wrapped with auto-generated proxies or serializable providers from the checkpoint. Internally, when the job is recovering, it rebuilds the graph on every JVM firstly, based on which all objects are regenerated.

Here is an example on how to do that:

```scala
trait Handler[T] {
    def handle(rdd: RDD[T]): Unit
}

class HandlerImpl extends Handler[ClickEvent] {
    override def handle(rdd: RDD[T]): Unit = { /* ... */ }
}

class HandlerModule extends SparkModule {
    def configure(): Unit = {
        bind[Handler[ClickEvent]].withSerializableProxy.to[HandlerImpl].in[SingletonScope]
    }
}

/* create the inject. Please refer the the sections above */
val injector = ...
 
val streamingContext = sparkSession.getOrCreateStreamingContext(checkpointPath, session => {
   // don't call the StreamingContext constructor directly
   val streamContext = session.newStreamingContext(Duration(1000*30))
   streamContext.checkpoint(checkpointPath)
   val eventStream: DStream[ClickEvent] = ...

   // handler here is a instance of a proxy class generated by neutrino
   val handler = injector.instance[Handler]
   eventStream.foreachRDD { rdd =>
       handler.handle(rdd)
   }
   streamContext
})
 
streamingContext.start()
streamingContext.awaitTermination()
```

In the above example, because `handler` is used during DAG building, it needs to be serializable for checkpoint. Neutrino will  handle that automatically if it is binded with proxy or `SerializableProvider`.

A full example can be found [here](./examples/src/main/scala/com/disneystreaming/neutrino/example/StreamingJobWithCheckpoint.scala).

# Scopes

## Singleton per JVM scope

The framework make it possible to keep a singeton object in an executor, which can be really useful in some cases.

For example, if we'd like to send a stream to a Kafka topic, it is necessary to keep a singleton KafkaProducer in each executor. Generally, this can be done with a static varaible or object instance in scala, which is difficult for testing.

But with the neutrino framework, we can easily get that by binding the Producer with a Singleton scope, which is easy for testing and maintenance.

Here is an example:

```scala
// producer is a proxy instance
val producer =  injector.instance[Producer[String, String]]
recordStream.foreachRDD { recordRDD =>
   recordRDD.foreach { record =>
       producer.send(record)
   }

```

Here is how to generate the kafka provider and bind these dependencies:

```scala
case class KafkaProducerConfig(properties: Map[String, Object])

// define how to generate a KafkaProducer instance
// kafkaProducerConfig can be read from config files
class KafkaProducerProvider @Inject()(kafkaProducerConfig: KafkaProducerConfig) 
extends Provider[Producer[String, String]] {
   override def get(): Producer[String, String] = {
       new KafkaProducer(kafkaProducerConfig.properties)
   }
}

class ConsumerModule(kafkaProducerConfig: KafkaProducerConfig)
extends SparkModule {
   override def configure(): Unit = {
       bind[KafkaProducerConfig].toInstance(kafkaProducerConfig)

       // bind the producer with a proxy generated
       // and `SingletonScope` means singleton per JVM
       bind[Producer[String, String]].withSerializableProxy
           .toProvider[KafkaProducerProvider].in[SingletonScope]
   }
}

```

## StreamingBatch scope

The StreamingBatch scope keeps the instance of a type singleton per streaming batch per JVM. See [here](./examples/src/main/scala/com/disneystreaming/neutrino/example/FilterModule.scala) for a full example.

# Other features

## Key spark objects are also injectable

These key spark objects such as SparkSession, SparkContext, StreamingContext are also injectable, which makes the spark application more flexible.

For example, with this feature, we can abstract the creation of a stream:

```scala
case class KafkaConsumerConfig(properties: Map[String, String], topics: Seq[String])
class UserFeatureLogStreamProvider @Inject()(
    streamingContext: StreamingContext,
    kafkaConfig: KafkaConsumerConfig)
   extends Provider[DStream[ClickEvent]] {
   override def get(): DStream[ClickEvent] = {
       KafkaUtils
           .createDirectStream[String, AvroClickEvent](
               streamingContext,
               LocationStrategies.PreferConsistent,
               kafkaConfig.properties,
               kafkaConfig.topics.toSet)
           .map(_.value())
           .map(avro => ClickEvent(avro.userId, avro.clickedItem))
   }
}
```

Then the stream can be retrived from the injector:

```scala
val stream = injector.instance[DStream[ClickEvent]]
```

## Injector Hierarchy / Child Injector

The Hierarchy/Child Injector is also supported in neutrino. Here is a simple example.

```scala
val injectorBuilder = sparkSession.newInjectorBuilder()
val rootInjector = injectorBuilder.newRootInjector(new ParentModule())
val childInjector rootInjector.createChildInjector(new ChildModule())

// this completeBuilding must be called after all injectors are built
injectorBuilder.completeBuilding()
```

## Multiple dependency graphs in a single job

In most cases, we only need a single dependency graph in a spark job, but if there is any necessity to separate the dependencies between to different graphs, the neutrino also provides a way to do that. All you need to do is to provide a different name for each graph. The name for the default graph is "\_\_default\_\_".

Here is an example

```scala
import com.disneystreaming.neutrino._

val defaultInjectorBuilder = sparkSession.newInjectorBuilder()
val injector1 = defaultInjectorBuilder.newRootInjector(new FilterModule(redisConfig))
injectorBuilder.completeBuilding()

val injectorBuilder2 = sparkSession.newInjectorBuilder("another graph")
val injector2 = injectorBuilder2.newRootInjector(new FilterModule(redisConfig))
injectorBuilder2.completeBuilding()
```

This feature may be useful in spark test cases. Under the test circumstances, a SparkContext object will be reused to run multiple test jobs, then different names have to be specified to differentiate them. An example can be found [here](./core/src/test/scala/com/disneystreaming/neutrino/StreamingBatchScopeTests.scala).
