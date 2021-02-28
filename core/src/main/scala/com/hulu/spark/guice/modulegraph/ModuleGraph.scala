package com.hulu.spark.guice.modulegraph

import com.google.inject.Injector

trait ModuleGraph {
    def injector(id: Int): Injector
    def property[T](name: String): Option[T]
}
