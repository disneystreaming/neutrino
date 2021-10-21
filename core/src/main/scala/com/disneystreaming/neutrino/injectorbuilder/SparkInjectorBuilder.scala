package com.disneystreaming.neutrino.injectorbuilder

import com.disneystreaming.neutrino.SparkInjector
import com.disneystreaming.neutrino.graph.{ModuleGraphBuilder, SerializableModuleGraphProvider}
import com.google.common.base.Preconditions
import com.disneystreaming.neutrino.{SerializableModule, SparkInjector}
import com.disneystreaming.neutrino.graph.{ModuleGraphBuilder, SerializableModuleGraphProvider}
import com.disneystreaming.neutrino.scope.StreamingBatchScopeModule
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ArrayBuffer

class SparkInjectorBuilder private[neutrino](private val sparkSession: SparkSession, private val name: String)
    extends SparkInjectorFactory {
    Preconditions.checkNotNull(sparkSession)

    private val graphBuilder = ModuleGraphBuilder.newBuilder()
    private val graphProxies = new ArrayBuffer[ModuleGraphProviderProxy]()

    def newRootInjector(modules: SerializableModule*): SparkInjector = {
        Preconditions.checkNotNull(modules)

        createInjector(-1, modules.+:(new SparkEnvironmentModule(sparkSession)).+:(new StreamingBatchScopeModule))
    }

    private[neutrino] override def createInjector(parentIndex: Int, modules: Seq[SerializableModule]): SparkInjector = {
        val providerProxy = new ModuleGraphProviderProxy
        val creator = if (parentIndex == -1) new SerializableProviderModuleCreator(providerProxy) else null
        val injectorIndex = graphBuilder.createChildInjector(parentIndex, modules, creator)
        val injector = new SparkInjectorImpl(providerProxy, injectorIndex, this)
        graphProxies += providerProxy
        injector
    }

    def prepareInjectors(): Unit = {
        val provider = SerializableModuleGraphProvider.createProvider(sparkSession, graphBuilder.build(), name)
        graphProxies.foreach(proxy => proxy.set(provider))
    }
}