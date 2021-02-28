package com.hulu.neutrino

import com.google.inject.Binder
import com.hulu.neutrino.lang.JAnnotation
import net.codingwell.scalaguice.InternalModule

import scala.language.experimental.macros

trait TypeInfoAutoBinding[B <: Binder] {
    outer: InternalModule[B] =>
    def bindWithTypeInfo[T, TScope <: JAnnotation](): Unit = macro TypeInfoAutoBindingMacro.bindWithSingleTypeInfo[T, TScope]

    //scalastyle:off
    def bindWithTypeInfo[TFrom, TTo <: TFrom, TScope <: JAnnotation](): Unit = macro TypeInfoAutoBindingMacro.bindWithTypeInfo[TFrom, TTo, TScope]

    //scalastyle:on

    def bindProviderWithTypeInfo[TFrom, TProvider <: javax.inject.Provider[_ <: TFrom], TScope <: JAnnotation](): Unit
        = macro TypeInfoAutoBindingMacro.bindProviderWithTypeInfo[TFrom, TProvider, TScope]
}
