package com.hulu.neutrino.injectorbuilder

import com.hulu.neutrino.{SerializableModule, SparkInjector}

trait SparkInjectorFactory {
    def createChildInjector(parentIndex: Int, modules: Seq[SerializableModule]): SparkInjector
}
