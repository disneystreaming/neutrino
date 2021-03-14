package com.hulu.neutrino.graph

import com.google.inject.Injector

private[neutrino] trait ModuleGraph {
    def injector(id: Int): Injector
    def property[T](name: String): Option[T]
}
