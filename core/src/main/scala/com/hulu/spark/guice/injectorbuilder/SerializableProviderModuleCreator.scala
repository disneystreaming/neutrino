package com.hulu.spark.guice.injectorbuilder

import com.hulu.spark.guice.SerializableModule
import com.hulu.spark.guice.modulegraph.{GraphProperties, ModuleGraphProvider, ModulesCreator}

class SerializableProviderModuleCreator(private val moduleGraphProvider: ModuleGraphProvider) extends ModulesCreator {
    override def create(graphProperties: GraphProperties): Seq[SerializableModule] = {
        Seq[SerializableModule](new SerializableProviderModule(
            new SerializableProviderFactory.IndexIncrementedSerializableProviderFactory(
                graphProperties,
                moduleGraphProvider)))
    }
}
