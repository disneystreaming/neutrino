package com.disneystreaming.neutrino.injectorbuilder

import com.disneystreaming.neutrino.{SerializableModule, SparkInjector}

private[neutrino] trait SparkInjectorFactory {
    private[neutrino] def createInjector(parentIndex: Int, modules: Seq[SerializableModule]): SparkInjector
}
