package com.disneystreaming.neutrino.injectorbuilder

import com.disneystreaming.neutrino.lang.JSerializable
import com.disneystreaming.neutrino.graph.{ModuleGraph, ModuleGraphProvider}

private[neutrino] class ModuleGraphProviderProxy extends ModuleGraphProvider with JSerializable {
    private var innerProvider: ModuleGraphProvider = _

    def set(provider: ModuleGraphProvider): Unit = innerProvider = provider

    override def moduleGraph: ModuleGraph = {
        if(this.innerProvider == null) {
            throw new IllegalStateException("The current model graph is not ready. " +
                "You may forget to call InjectorBuilder.completeBuilding")
        }

        innerProvider.moduleGraph
    }
}
