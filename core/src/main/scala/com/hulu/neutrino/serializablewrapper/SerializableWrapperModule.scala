package com.hulu.neutrino.serializablewrapper

import com.google.inject.Binder
import com.hulu.neutrino.`macro`.SerializableWrapperModuleMacro
import net.codingwell.scalaguice.InternalModule
import net.codingwell.scalaguice.ScalaModule.ScalaLinkedBindingBuilder

import scala.language.experimental.macros

trait SerializableWrapperModule[B <: Binder] {
    module: InternalModule[B] =>

    implicit class ScalaLinkedBindingBuilderSerializableWrapper[T](private val builder: ScalaLinkedBindingBuilder[T]) {
        def withSerializableProxy: ScalaLinkedBindingBuilder[T] = macro SerializableWrapperModuleMacro.withSerializableProxyImpl[T]
    }
}
