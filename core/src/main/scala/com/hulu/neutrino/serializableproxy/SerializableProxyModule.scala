package com.hulu.neutrino.serializableproxy

import com.google.inject.Binder
import com.hulu.neutrino.`macro`.SerializableProxyModuleMacro
import net.codingwell.scalaguice.InternalModule
import net.codingwell.scalaguice.ScalaModule.ScalaLinkedBindingBuilder

import scala.language.experimental.macros

trait SerializableProxyModule[B <: Binder] {
    module: InternalModule[B] =>

    implicit class ScalaLinkedBindingBuilderSerializableWrapper[T](private val builder: ScalaLinkedBindingBuilder[T]) {
        def withSerializableProxy: ScalaLinkedBindingBuilder[T] = macro SerializableProxyModuleMacro.withSerializableProxyImpl[T]
    }
}
