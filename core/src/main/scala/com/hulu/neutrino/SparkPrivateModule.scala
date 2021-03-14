package com.hulu.neutrino

import com.google.inject.{PrivateBinder, PrivateModule}
import com.hulu.neutrino.lang.JSerializable
import com.hulu.neutrino.serializableprovider.InjectableSerializableProviderModule
import com.hulu.neutrino.serializableproxy.SerializableProxyModule

abstract class SparkPrivateModule
    extends PrivateModule
        with ScalaPrivateModule
        with JSerializable
        with SerializableProxyModule[PrivateBinder]
        with InjectableSerializableProviderModule[PrivateBinder]
