package com.hulu.spark.guice.injectorbuilder

import com.google.common.base.Preconditions
import com.google.inject.Key
import com.hulu.spark.guice.modulegraph.ModuleGraphProvider
import com.hulu.spark.guice.{SerializableModule, SparkInjector}

private[guice] class SparkInjectorImpl (
    private val graphProvider: ModuleGraphProvider,
    private val injectorIndex: Int,
    @transient private val sparkInjectorFactory: SparkInjectorFactory) extends SparkInjector {

    override def createChildInjector(modules: SerializableModule*): SparkInjector = {
        Preconditions.checkNotNull(modules)
        if (modules.size <= 0) {
            throw new IllegalArgumentException("models should not be empty")
        }

        if (sparkInjectorFactory == null) {
            throw new RuntimeException("createChildInjector should only be called in master")
        }

        sparkInjectorFactory.createChildInjector(injectorIndex, modules.toSeq)
    }

    override def instanceByKey[T](key: Key[T]): T = graphProvider.moduleGraph.injector(injectorIndex).getInstance(key)
}

