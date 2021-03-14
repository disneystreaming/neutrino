package com.hulu.neutrino.injectorbuilder

import com.hulu.neutrino.SerializableModule
import com.hulu.neutrino.graph.{GraphProperties, ModuleGraphProvider, ModulesCreator}

private[neutrino] class SerializableProviderModuleCreator(private val moduleGraphProvider: ModuleGraphProvider) extends ModulesCreator {
    override def create(graphProperties: GraphProperties): Seq[SerializableModule] = {
        Seq[SerializableModule](new SerializableProviderTypeListenerModule(
            new IndexIncrementedSerializableProviderFactory(
                graphProperties,
                moduleGraphProvider)))
    }
}
