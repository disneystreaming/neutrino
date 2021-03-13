package com.hulu.neutrino

import com.google.inject.{PrivateBinder, PrivateModule}
import com.hulu.neutrino.lang.JSerializable
import com.hulu.neutrino.serializableprovider.InjectableSerializableProviderModule
import com.hulu.neutrino.serializablewrapper.SerializableWrapperModule

abstract class SparkPrivateModule
    extends PrivateModule
        with ScalaPrivateModule
        with JSerializable
        with SerializableWrapperModule[PrivateBinder]
        with InjectableSerializableProviderModule[PrivateBinder]
