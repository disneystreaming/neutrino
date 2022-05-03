package com.disneystreaming.neutrino

/**
 * build the [[com.disneystreaming.neutrino.SparkInjector]] instances
 */
trait InjectorBuilder {
    /**
     * build the root injector for the given set of modules
     * @param modules the module set to describe the bindings of the graph
     * @return the root injector built from the given set of modules
     */
    def newRootInjector(modules: SerializableModule*): SparkInjector

    /**
     * mark the completion of injectors (including both root injector and other child injectors)
     * Note: This method MUST be called before creating instance from these injectors
     */
    def completeBuilding(): Unit
}
