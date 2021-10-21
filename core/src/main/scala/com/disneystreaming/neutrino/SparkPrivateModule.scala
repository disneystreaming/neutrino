package com.disneystreaming.neutrino

import com.disneystreaming.neutrino.serializableprovider.InjectableSerializableProviderModule
import com.disneystreaming.neutrino.serializableproxy.SerializableProxyModule
import com.google.inject.{PrivateBinder, PrivateModule}
import com.disneystreaming.neutrino.lang.JSerializable
import com.disneystreaming.neutrino.serializableprovider.InjectableSerializableProviderModule
import com.disneystreaming.neutrino.serializableproxy.SerializableProxyModule

abstract class SparkPrivateModule
    extends PrivateModule
        with ScalaPrivateModule
        with JSerializable
        with SerializableProxyModule[PrivateBinder]
        with InjectableSerializableProviderModule[PrivateBinder]
