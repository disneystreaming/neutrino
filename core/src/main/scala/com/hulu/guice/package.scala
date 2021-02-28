package com.hulu

import com.google.inject.TypeLiteral
import com.google.inject.binder.{LinkedBindingBuilder, ScopedBindingBuilder}
import javax.inject.Provider

import _root_.scala.reflect.runtime.universe._

package object guice {
    type SingletonScope = com.google.inject.Singleton

    def typeLiteral[T : TypeTag]: TypeLiteral[T] = net.codingwell.scalaguice.typeLiteral[T]

    implicit class ScalaLinkedBindingBuilderExtensions[T](private val b: LinkedBindingBuilder[T]) extends AnyVal {
        def toProviderTypeTag[TProvider <: Provider[_ <: T] : TypeTag]: ScopedBindingBuilder = b.toProvider(typeLiteral[TProvider])
    }
}
