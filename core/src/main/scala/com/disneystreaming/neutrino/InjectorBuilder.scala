package com.disneystreaming.neutrino

trait InjectorBuilder {
    def newRootInjector(modules: SerializableModule*): SparkInjector

    def completeBuilding(): Unit
}
