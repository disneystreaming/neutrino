package com.disneystreaming.neutrino

import com.google.inject.Key
import com.disneystreaming.neutrino.lang.JSerializable

/**
 * It is similar to [[com.google.inject.Injector]]
 * and can be used to create objects from the dependency graph.
 * It is serializable and can be transferred to executors,
 * and will always reference the graph in current JVM.
 */
trait SparkInjector extends JSerializable {

    /**
     * Returns the appropriate instance for the given injection key.
     * See [[com.google.inject.Key]] about how to build a key
     * @param key the binding key
     * @tparam T the instance type
     * @return the instance built from the graph
     */
    def instanceByKey[T](key: Key[T]): T

    /**
     * Creates an injector for the given set of modules.
     * @param modules the module set to describe the bindings of the graph
     * @return an injector built from the module set
     */
    def createChildInjector(modules: SerializableModule*): SparkInjector
}
