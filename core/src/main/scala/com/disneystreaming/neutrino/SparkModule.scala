package com.disneystreaming.neutrino

import com.disneystreaming.neutrino.serializableprovider.InjectableSerializableProviderModule
import com.disneystreaming.neutrino.serializableproxy.SerializableProxyModule
import com.google.inject.{AbstractModule, Binder}
import com.disneystreaming.neutrino.lang.JSerializable
import com.disneystreaming.neutrino.serializableprovider.InjectableSerializableProviderModule
import com.disneystreaming.neutrino.serializableproxy.SerializableProxyModule

abstract class SparkModule
    extends AbstractModule
        with ScalaModule
        with JSerializable
        with SerializableProxyModule[Binder]
        with InjectableSerializableProviderModule[Binder]
