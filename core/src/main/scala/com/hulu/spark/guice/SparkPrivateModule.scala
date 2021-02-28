package com.hulu.spark.guice

import com.google.inject.{PrivateBinder, PrivateModule}
import com.hulu.spark.guice.binding.ScopeExtension
import com.hulu.spark.guice.serializablewrapper.SerializableWrapperModule

abstract class SparkPrivateModule
    extends PrivateModule
        with ScalaPrivateModule
        with SerializableModule
        with SerializableWrapperModule[PrivateBinder]
        with TypeInfoAutoBinding[PrivateBinder]
        with TypeInfoBinding[PrivateBinder]
        with ScopeExtension
