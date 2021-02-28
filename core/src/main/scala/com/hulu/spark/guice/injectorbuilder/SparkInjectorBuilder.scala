package com.hulu.spark.guice.injectorbuilder

import com.google.common.base.Preconditions
import com.hulu.spark.guice.modulegraph.{ModuleGraphBuilder, SerializableModuleGraphProvider}
import com.hulu.spark.guice.scope.StreamingBatchScopeModule
import com.hulu.spark.guice.{SerializableModule, SparkEnvironmentModule, SparkInjector}
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ArrayBuffer

class SparkInjectorBuilder private[guice](private val sparkSession: SparkSession, private val name: String)
    extends SparkInjectorFactory {
    Preconditions.checkNotNull(sparkSession)

    private val graphBuilder = ModuleGraphBuilder.newBuilder()
    private val graphProxies = new ArrayBuffer[ModuleGraphProviderProxy]()

    def newRootInjector(modules: SerializableModule*): SparkInjector = {
        Preconditions.checkNotNull(modules)

        createChildInjector(-1, modules.+:(new SparkEnvironmentModule(sparkSession)).+:(new StreamingBatchScopeModule))
    }

    override def createChildInjector(parentIndex: Int, modules: Seq[SerializableModule]): SparkInjector = {
        val providerProxy = new ModuleGraphProviderProxy
        val creator = if (parentIndex == -1) new SerializableProviderModuleCreator(providerProxy) else null
        val injectorIndex = graphBuilder.createChildInjector(parentIndex, modules, creator)
        val injector = new SparkInjectorImpl(providerProxy, injectorIndex, this)
        graphProxies += providerProxy
        injector
    }

    def prepareInjectors(): Unit = {
        val provider = SerializableModuleGraphProvider.createProvider(sparkSession.sparkContext, graphBuilder.build(), name)
        graphProxies.foreach(proxy => proxy.set(provider))
    }
}
