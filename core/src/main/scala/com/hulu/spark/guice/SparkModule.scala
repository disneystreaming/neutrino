package com.hulu.spark.guice

import com.google.inject.{AbstractModule, Binder}
import com.hulu.spark.guice.binding.ScopeExtension
import com.hulu.spark.guice.serializablewrapper.SerializableWrapperModule

abstract class SparkModule
    extends AbstractModule
        with ScalaModule
        with SerializableModule
        with SerializableWrapperModule[Binder]
        with ScopeExtension
        with TypeInfoAutoBinding[Binder]
