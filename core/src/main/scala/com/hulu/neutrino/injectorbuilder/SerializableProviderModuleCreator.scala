package com.hulu.neutrino.injectorbuilder

import com.hulu.neutrino.SerializableModule
import com.hulu.neutrino.modulegraph.{GraphProperties, ModuleGraphProvider, ModulesCreator}

class SerializableProviderModuleCreator(private val moduleGraphProvider: ModuleGraphProvider) extends ModulesCreator {
    override def create(graphProperties: GraphProperties): Seq[SerializableModule] = {
        Seq[SerializableModule](new SerializableProviderModule(
            new SerializableProviderFactory.IndexIncrementedSerializableProviderFactory(
                graphProperties,
                moduleGraphProvider)))
    }
}
