package com.hulu.neutrino

import com.google.inject.{AbstractModule, Binder}
import com.hulu.neutrino.lang.JSerializable
import com.hulu.neutrino.serializableprovider.InjectableSerializableProviderModule
import com.hulu.neutrino.serializableproxy.SerializableProxyModule

abstract class SparkModule
    extends AbstractModule
        with ScalaModule
        with JSerializable
        with SerializableProxyModule[Binder]
        with InjectableSerializableProviderModule[Binder]
