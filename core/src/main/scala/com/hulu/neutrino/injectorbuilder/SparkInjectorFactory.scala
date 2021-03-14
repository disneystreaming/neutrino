package com.hulu.neutrino.injectorbuilder

import com.hulu.neutrino.{SerializableModule, SparkInjector}

private[neutrino] trait SparkInjectorFactory {
    private[neutrino] def createInjector(parentIndex: Int, modules: Seq[SerializableModule]): SparkInjector
}
