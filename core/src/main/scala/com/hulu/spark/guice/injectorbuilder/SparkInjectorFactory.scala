package com.hulu.spark.guice.injectorbuilder

import com.hulu.spark.guice.{SerializableModule, SparkInjector}

trait SparkInjectorFactory {
    def createChildInjector(parentIndex: Int, modules: Seq[SerializableModule]): SparkInjector
}
