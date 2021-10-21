package com.disneystreaming.neutrino.injectorbuilder

import com.disneystreaming.neutrino.graph.{GraphProperties, ModulesCreator}
import com.disneystreaming.neutrino.SerializableModule
import com.disneystreaming.neutrino.graph.{GraphProperties, ModuleGraphProvider, ModulesCreator}

private[neutrino] class SerializableProviderModuleCreator(private val moduleGraphProvider: ModuleGraphProvider) extends ModulesCreator {
    override def create(graphProperties: GraphProperties): Seq[SerializableModule] = {
        Seq[SerializableModule](new SerializableProviderTypeListenerModule(
            new IndexIncrementedSerializableProviderFactory(
                graphProperties,
                moduleGraphProvider)))
    }
}
