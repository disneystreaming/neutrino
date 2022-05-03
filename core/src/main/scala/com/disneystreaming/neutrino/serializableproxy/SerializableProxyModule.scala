package com.disneystreaming.neutrino.serializableproxy

import com.disneystreaming.neutrino.`macro`.SerializableProxyModuleMacro
import com.google.inject.Binder
import com.disneystreaming.neutrino.`macro`.SerializableProxyModuleMacro
import net.codingwell.scalaguice.InternalModule
import net.codingwell.scalaguice.ScalaModule.ScalaLinkedBindingBuilder

import scala.language.experimental.macros

trait SerializableProxyModule[B <: Binder] {
    module: InternalModule[B] =>

    implicit class ScalaLinkedBindingBuilderSerializableWrapper[T](private val builder: ScalaLinkedBindingBuilder[T]) {
        /**
         * generate serializable proxy for binding.
         * <br/> The binding key (including binding type and annotation) should be specified before calling this method,
         * and the implementation (implementation class type, provider instance or provider type) is after the calling
         * <br/> Usage:
         * <br/> 1. Bind type
         * {{{ bind[T].withSerializableProxy.to[TImpl].in[Scope] }}}
         * <br/> 2. Bind type with annotation instance
         * {{{ bind[T].annotatedWith(ann).withSerializableProxy.to[TImpl].in[Scope] }}}
         * <br/> 3. Bind type with annotation type
         * {{{ bind[T].annotatedWith[TAnn].withSerializableProxy.to[TImpl].in[Scope] }}}
         * <br/> In private modules, the binding can be exposed as normal. Example:
         * {{{
         *     expose(key)
         *     // or
         *     expose[T]
         *     // or
         *     expose[T].annotatedWith[TAnnotation]
         *     or
         *     expose[T].annotationWith(annotation)
         * }}}
         * @return [[ScalaLinkedBindingBuilder]] instance can be used to specify the way to create the instance and scope
         */
        def withSerializableProxy: ScalaLinkedBindingBuilder[T] = macro SerializableProxyModuleMacro.withSerializableProxyImpl[T]
    }
}
