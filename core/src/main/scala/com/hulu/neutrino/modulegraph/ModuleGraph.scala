package com.hulu.neutrino.modulegraph

import com.google.inject.Injector

trait ModuleGraph {
    def injector(id: Int): Injector
    def property[T](name: String): Option[T]
}
