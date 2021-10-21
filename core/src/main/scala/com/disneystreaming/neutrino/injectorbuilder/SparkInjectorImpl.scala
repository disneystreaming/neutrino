package com.disneystreaming.neutrino.injectorbuilder

import com.disneystreaming.neutrino.SparkInjector
import com.google.common.base.Preconditions
import com.google.inject.Key
import com.disneystreaming.neutrino.graph.ModuleGraphProvider
import com.disneystreaming.neutrino.{SerializableModule, SparkInjector}

private[neutrino] class SparkInjectorImpl (
    private val graphProvider: ModuleGraphProvider,
    private val injectorIndex: Int,
    @transient private val sparkInjectorFactory: SparkInjectorFactory) extends SparkInjector {

    override def createChildInjector(modules: SerializableModule*): SparkInjector = {
        Preconditions.checkNotNull(modules)
        if (modules.size <= 0) {
            throw new IllegalArgumentException("models should not be empty")
        }

        if (sparkInjectorFactory == null) {
            throw new UnsupportedOperationException("createChildInjector should only be called in master")
        }

        sparkInjectorFactory.createInjector(injectorIndex, modules.toSeq)
    }

    override def instanceByKey[T](key: Key[T]): T = graphProvider.moduleGraph.injector(injectorIndex).getInstance(key)
}

