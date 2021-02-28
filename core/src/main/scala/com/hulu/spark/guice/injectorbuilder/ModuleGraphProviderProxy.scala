package com.hulu.spark.guice.injectorbuilder

import com.hulu.spark.JSerializable
import com.hulu.spark.guice.modulegraph.{ModuleGraph, ModuleGraphProvider}

class ModuleGraphProviderProxy extends ModuleGraphProvider with JSerializable {
    private var innerProvider: ModuleGraphProvider = _

    def set(provider: ModuleGraphProvider): Unit = innerProvider = provider

    override def moduleGraph: ModuleGraph = innerProvider.moduleGraph
}
