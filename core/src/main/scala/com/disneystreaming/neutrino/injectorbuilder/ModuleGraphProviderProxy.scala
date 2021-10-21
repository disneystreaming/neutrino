package com.disneystreaming.neutrino.injectorbuilder

import com.disneystreaming.neutrino.lang.JSerializable
import com.disneystreaming.neutrino.graph.{ModuleGraph, ModuleGraphProvider}

private[neutrino] class ModuleGraphProviderProxy extends ModuleGraphProvider with JSerializable {
    private var innerProvider: ModuleGraphProvider = _

    def set(provider: ModuleGraphProvider): Unit = innerProvider = provider

    override def moduleGraph: ModuleGraph = {
        if(this.innerProvider == null) {
            throw new RuntimeException("The inner ModuleGraphProvider is ready")
        }

        innerProvider.moduleGraph
    }
}
