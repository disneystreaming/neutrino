package com.hulu.spark.guice

import com.google.inject.Key

trait SparkInjector extends Serializable {
    def instanceByKey[T](key: Key[T]): T

    def createChildInjector(modules: SerializableModule*): SparkInjector
}
