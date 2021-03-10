# neutrino

A dependency injection (DI) framework for apache spark

# Why it is so difficult to apply DI on apache spark

As we know, [dependency injection](https://en.wikipedia.org/wiki/Dependency_injection) (DI) is a famous design pattern that is widely used in Object-Oriented Programming (OOP). It separates the responsibility of "use" from the responsibility of "construction", and keeps modules evolving independently.

There are some mature dependency injection frameworks in JVM world, such as [Guice](https://github.com/google/guice) and [Spring framework](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html), which are all designed to work properly in a single JVM process.

A spark job is a distributed application which requires collaboration of multiple JVMs. Under spark circumstances, it is so common to pass some object from the driver to executors, and spark requires the passed object and all its direct or in-direct dependencies to be serializable (as described by the following picture), which needs quite a lot of efforts. Not to mention, if you enabled the checkpoint in spark streaming, more object need to be serialized. Normal DI framework can't handle it for us.

![serialize all dependencies](./images/deps_serialization.png)

# What can the neutrino framework do
The neutrino framework is designed to relieve the serialization effort work in spark application. In fact, in most cases, our framework will handle the serialization/deserialization work automatically (include normal object serialization and checkpoint).

And the framework also provides some handy DI object scope management features, such as Singleton Scope per JVM, StreamingBatch scope (reuse the object in the same spark streaming batch per JVM).
In addition, the spark utility object such as SparkContext, SparkSession, StreamingContext are also injectable, which provides more flexibility for the orchestration of the spark job.

# How does the neutrino handle the serialization problem

As we know, to adopt the DI framework, we need to first build a dependency graph first, which describes the dependency relationship between multiple instances. Guice uses Module API to build the graph while the Spring framework uses XML file or annotation.
The neutrino internally builds the dependency graph with the Guice framework. After that, it broadcasts the graph to every executor, which means each executor JVM has a dependency graph, as described by the picture below.

![serialize creation method](./images/serialize_creation_method.png)

If an object is about to be passed to another JVM, instead of serializing the object and its dependencies, the neutrino framework remembers the creation method of the object and passes the information to the target JVM and recreates it there in the same way, The object even doesn't have to be serializable, all of which is done automatically by the framework.
Here is an example:

## Example: handle serialization automatically
Consider such a case, if we'd like to abstract the logic to filter the event stream based on a white list of user id, which is stored in the database. Here is how we handle it with the neutrino.

```scala
import javax.inject.Inject
import scala.collection.mutable

case class TestEvent(userId: String)

trait EventFilter[T] {
    def filter(t: T): Boolean
}

// here is the spark job logic
val injectorBuilder = sparkSession.newInjectorBuilder()
val injector = injectorBuilder.newRootInjector(new FilterModule(dbConfig))
injectorBuilder.prepareInjectors() // Don't forget to call this before getting any instance from injector

val filter = injector.instance[EventFilter[TestEvent]]
val eventStream: DStream[TestEvent] = ...
eventStream.filter(e => filter.filter(e))

// OR
DStream[TestEvent]
    .filter(e => injector.instance[EventFilter[TestEvent]].filter(e))
```
In normal case, the `EventFilter[TestEvent]` instance must implement the `java.io.Serializable` interface since it is passed from the driver to executors.
But with neutrino, we don't need to do that.
```scala
class DbUserWhiteListsEventFilter @Inject()(dbConnection: java.sql.Connection) extends EventFilter[TestEvent] {
    private lazy val userIdSet = {
        getAllUserIds(dbConnection)
    }

    override def filter(t: TestEvent): Boolean = {
        t.userId != null && userIdSet.contains(t.userId)
    }
}

// how to generate the db connection
case class DbConfig(url: String, userName: String, password: String)
class DbConnectionProvider @Inject()(dbConfig: DbConfig) extends Provider[java.sql.Connection] {
    override def get(): Connection = {
        DriverManager.getConnection(
            dbConfig.url,
            dbConfig.userName,
            dbConfig.password);
    }
}
```
Here is how we bind the dependencies with neutrino.
```scala
class FilterModule(dbConfig: DbConfig) extends SparkModule {
    override def configure(): Unit = {
        bind[DbConfig].toInstance(dbConfig)
        bind[java.sql.Connection].toProvider[DbConnectionProvider].in[SingletonScope]
        // the magic here
        bind[EventFilter[TestEvent]].withSerializableWrapper.to[DbUserWhiteListsEventFilter].in[SingletonScope]
    }
}
```
The extension method `withSerializableWrapper` will generate a serializable wrapper with the same interface (`EventFilter[TestEvent]`), which is small and contains the creation info of the actually object. When it is passed across multiple JVMs, it is serialized and creates the same object with the dependency graph on the target JVMs after deserialization.

And since the scope for the object is `SingletonScope` (singleton per JVM), the same object would be reused if there is already one there, which utilizes the Guice container scope mechanism effectively.

In the example above, `DbConnectionProvider` would be created with the graph per JVM, so all its dependencies even the `java.sql.Connection` can be injected, which will be created per JVM with the `DbConnectionProvider`.

**There is only one limitation** --- the binding module which creates the dependency graph need to be serializable (the base class `SparkModule` extends the `java.io.Serializable`), which is easy to handle. For the above example, the only thing need to be serialized is `DbConfig`.

## Example: recover the job from spark checkpoint
To ensure the stability of spark streaming job, we need to enable the checkpoint in case of job failure, which requires any closure object need to be serializable. The neutrino framework would automatically handle the recovering work for all objects generated from the graph. Actually, when the job is recovering, it rebuilds the graph on every JVM firstly, on which all objects are regenerated.
Here is an example of how to do that:
```scala
import com.hulu.neutrino._

val injectorBuilder = sparkSession.newInjectorBuilder()
val injector = injectorBuilder.newRootInjector(new FilterModule(dbConfig))
injectorBuilder.prepareInjectors() // Don't forget to call this before getting any instance from injector

val checkpointPath = "hdfs://HOST/checkpointpath"

// Don't call StreamingContext.getOrCreate directly
val streamingContext = sparkSession.getOrCreateStreamingContext(checkpointPath, session => {
    // Don't call the constructor directly
    val streamContext = session.newStreamingContext(Duration(1000*30))
    streamContext.checkpoint(checkpointPath)
    val eventStream: DStream[TestEvent] = ...
    eventStream
        .filter(e => rootInjector.instance[EventFilter[TestEvent]].filter(e))
    streamContext
})

streamingContext.start()
streamingContext.awaitTermination()
```

## Advanced usage for object passing-around between JVMs
The auto-generated serializable wrapper assumes the binding type is an interface or trait in scala. If it is not the case, like some concrete or final class need to be passed to executors, the neutrino framework also provide a way to get the serializable info as a `Provider[T]` (which is serializable) , you can pass the actual object with it.
Here is an example:
```scala
final class EventProcessor {
    def process(event: TestEvent)
}

class StreamHandler extends EventFilter[TestEvent] {
    private var provider: Provider[EventProcessor]
    
    @InjectSerializableProvider
    def setProvider(provider: Provider[EventProcessor]): Unit = {
        this.provider = provider
    }
    
    def handleStream(eventStream: DStream[TestEvent]): Unit = {
        val localProvider = provider
        eventStream.map { e =>
            localProvider.get().process(e)
        }
    }
}
```

Currently, the serializable `Provider[T]` can only be retrieved via annotation `InjectSerializableProvider` on a setter method.

# new scopes
## Example: StreamingBatch scope
 If we evolve the example above a little, say the user white list in the database is changeable, and we'd like to update the white list data in every batch. To achieve this goal, we only need to do a little change in the module.
```scala
class FilterModule(dbConfig: DbConfig) extends SparkModule {
    override def configure(): Unit = {
        // the same as the above example
        bind[DbConfig].toInstance(dbConfig)
        bind[java.sql.Connection].toProvider[DbConnectionProvider].in[SingletonScope]
        // just change the scope from SingletonScope to StreamingBatch
        bind[EventFilter[TestEvent]].withSerializableWrapper.to[DbUserWhiteListsEventFilter].in[StreamingBatch]
    }
}
```
With the `StreamingBatch` scope, the instance for `EventFilter[TestEvent]` will be created per streaming batch, and reuse it within the same batch. So the white list data will be reloaded each batch.

# Other features
## Some key spark objects are also injectable
These injectable objects include SparkSession, SparkContext, StreamingContext, which makes the spark application more flexible.
With it, we can even make `DStream[T]` or `RDD[T]` injectable. [Here](./examples/src/main/scala/com/hulu/neutrino/example/TestEventStreamProvider.scala) is an example. 

## Injector Hierarchy / Child Injector
The Injector Hierarchy/Child Injector is also supported. Here is a simple example.
```scala
val injectorBuilder = sparkSession.newInjectorBuilder()
val rootInjector = injectorBuilder.newRootInjector(new ParentModule())
val childInjector rootInjector.createChildInjector(new ChildModule())
// this prepareInjectors must be called after all injectors are built and before any instance is retrieved from any injector
injectorBuilder.prepareInjectors()
```
Note: All children injectors belonged to the same root injector are in the same dependency graph

## multiple dependency graphs in single job
In most cases, we only need a single dependency graph in a spark job, but if there is any necessary to separate the dependency between logic, the neutrino also provide a way to create separate graphs. All you need to do is provide a different name for each graph. The name for the default graph is "default".
Here is an example
```scala
import com.hulu.neutrino._

val defaultInjectorBuilder = sparkSession.newInjectorBuilder()
val injector1 = defaultInjectorBuilder.newRootInjector(new FilterModule(dbConfig))
injectorBuilder.prepareInjectors()

val injectorBuilder2 = sparkSession.newInjectorBuilder("another graph")
val injector2 = injectorBuilder2.newRootInjector(new FilterModule(dbConfig))
injectorBuilder.prepareInjectors()

// any spark logic
```