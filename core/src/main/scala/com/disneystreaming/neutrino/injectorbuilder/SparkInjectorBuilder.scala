package com.disneystreaming.neutrino.injectorbuilder

import com.disneystreaming.neutrino.graph.{ModuleGraphBuilder, SerializableModuleGraphProvider}
import com.disneystreaming.neutrino.scope.StreamingBatchScopeModule
import com.disneystreaming.neutrino.{InjectorBuilder, SerializableModule, SparkInjector}
import com.google.common.base.Preconditions
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ArrayBuffer

class SparkInjectorBuilder private[neutrino](private val sparkSession: SparkSession, private val name: String)
    extends SparkInjectorFactory with InjectorBuilder {
    Preconditions.checkNotNull(sparkSession)

    private val graphBuilder = ModuleGraphBuilder.newBuilder()
    private val graphProxies = new ArrayBuffer[ModuleGraphProviderProxy]()
    private var buildComplete = false
    private var rootBuilt = false

    override def newRootInjector(modules: SerializableModule*): SparkInjector = {
        Preconditions.checkNotNull(modules)
        checkBuildComplete()
        if (rootBuilt) {
            throw new IllegalStateException("root inject is only allowed to create once for each builder")
        }

        val injector = createInjector(-1, modules.+:(new SparkEnvironmentModule(sparkSession)).+:(new StreamingBatchScopeModule))

        rootBuilt = true

        injector
    }

    private[neutrino] override def createInjector(parentIndex: Int, modules: Seq[SerializableModule]): SparkInjector = {
        checkBuildComplete()

        val providerProxy = new ModuleGraphProviderProxy
        val creator = if (parentIndex == -1) new SerializableProviderModuleCreator(providerProxy) else null
        val injectorIndex = graphBuilder.createChildInjector(parentIndex, modules, creator)
        val injector = new SparkInjectorImpl(providerProxy, injectorIndex, this)
        graphProxies += providerProxy
        injector
    }

    override def completeBuilding(): Unit = {
        checkBuildComplete()

        val provider = SerializableModuleGraphProvider.createProvider(sparkSession, graphBuilder.build(), name)
        graphProxies.foreach(proxy => proxy.set(provider))
        buildComplete = true
    }

    private def checkBuildComplete(): Unit = {
        if (buildComplete) {
            throw new IllegalStateException("the injector building is already completed, " +
                "and it is not allowed to create new root/child injector or completeBuilding after that.")
        }
    }
}
