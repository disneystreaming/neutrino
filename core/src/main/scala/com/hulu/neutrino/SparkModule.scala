package com.hulu.neutrino

import com.google.inject.{AbstractModule, Binder}
import com.hulu.neutrino.lang.JSerializable
import com.hulu.neutrino.serializableprovider.InjectableSerializableProviderModule
import com.hulu.neutrino.serializablewrapper.SerializableWrapperModule

abstract class SparkModule
    extends AbstractModule
        with ScalaModule
        with JSerializable
        with SerializableWrapperModule[Binder]
        with InjectableSerializableProviderModule[Binder]
