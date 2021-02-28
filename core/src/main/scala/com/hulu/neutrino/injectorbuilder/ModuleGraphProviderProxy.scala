package com.hulu.neutrino.injectorbuilder

import com.hulu.neutrino.lang.JSerializable
import com.hulu.neutrino.modulegraph.{ModuleGraph, ModuleGraphProvider}

class ModuleGraphProviderProxy extends ModuleGraphProvider with JSerializable {
    private var innerProvider: ModuleGraphProvider = _

    def set(provider: ModuleGraphProvider): Unit = innerProvider = provider

    override def moduleGraph: ModuleGraph = innerProvider.moduleGraph
}
