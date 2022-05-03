package com.disneystreaming.neutrino

import com.disneystreaming.neutrino.serializableprovider.InjectableSerializableProviderModule
import com.disneystreaming.neutrino.serializableproxy.SerializableProxyModule
import com.google.inject.{PrivateBinder, PrivateModule}
import com.disneystreaming.neutrino.lang.JSerializable
import com.disneystreaming.neutrino.serializableprovider.InjectableSerializableProviderModule
import com.disneystreaming.neutrino.serializableproxy.SerializableProxyModule

/**
 * The abstract super class for private modules.
 * It extends [[com.google.inject.PrivateModule]] and [[java.io.Serializable]]
 * and provide methods to bind the proxy and [[com.disneystreaming.neutrino.SerializableProvider]]
 */
abstract class SparkPrivateModule
    extends PrivateModule
        with ScalaPrivateModule
        with JSerializable
        with SerializableProxyModule[PrivateBinder]
        with InjectableSerializableProviderModule[PrivateBinder]
