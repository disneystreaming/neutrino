# neutrino

A dependency injection (DI) framework for apache spark

# Why it is so difficult to apply DI on apache spark

As we know, [dependency injection](https://en.wikipedia.org/wiki/Dependency_injection) (DI) is a famous design pattern that is widely used in Object-Oriented Programming (OOP). It separates the responsibility of "use" from the responsibility of "construction", and keeps modules evolving independently.

There are some mature dependency injection frameworks in JVM world, such as [Guice](https://github.com/google/guice) and [Spring framework](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html), which are all designed to work properly in a single JVM process.

A spark job is a distributed application which requires collaboration of multiple JVMs. Under spark circumstances, it is so common to pass some object from the driver to executors, and spark requires the passed object and all its direct or in-direct dependencies to be serializable (as described by the following picture), which needs quite a lot of efforts. Not to mention, if you enabled the checkpoint in spark streaming, more object need to be serialized. Normal DI framework can't handle it for us.

`/*need to add a picture here*/`
# What can the neutrino framework do
The neutrino framework is designed to relieve the serialization effort work in spark application. In fact, in most cases, our framework will handle the serialization/deserialization work automatically (include normal object serialization and checkpoint).
And the framework also provides some handy DI object scope management features, such as Singleton Scope per JVM, StreamingBatch scope (reuse the object in the same spark streaming batch per JVM).
In addition, the spark utility object such as SparkContext, SparkSession, StreamingContext are also injectable, which provides more flexibility for the orchestration of the spark job.

# How does the neutrino handle the serialization problem

As we know, to adopt the DI framework, we need to first build a dependency graph first, which describes the dependency relationship between multiple instances. Guice uses Module API to build the graph while the Spring framework uses XML file or annotation.
The neutrino internally builds the dependency graph with the Guice framework. After that, it broadcasts the graph to every executor, which means each executor JVM has a dependency graph, as described by the picture below.

`/*need to add a picture here*/`

If an object is about to be passed to another JVM, instead of serializing the object and its dependencies, the neutrino framework remembers the creation method of the object and passes the information to the target JVM and recreates it there in the same way, The object even doesn't have to be serializable, all of which is done automatically by the framework.
Here is an example:

## Example
