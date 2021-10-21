package com.disneystreaming.neutrino.injectorbuilder

import com.disneystreaming.neutrino.ScalaModule
import com.disneystreaming.neutrino.serializableprovider.SerializableProviderFactory
import com.google.inject.matcher.Matchers
import com.disneystreaming.neutrino.ScalaModule
import com.disneystreaming.neutrino.lang.JSerializable
import com.disneystreaming.neutrino.serializableprovider.{SerializableProviderFactory, SerializableProviderTypeListener}

private[neutrino] class SerializableProviderTypeListenerModule(serializableProviderFactory: SerializableProviderFactory)
    extends ScalaModule with JSerializable {
    override def configure(): Unit = {
        bindListener(Matchers.any, new SerializableProviderTypeListener(serializableProviderFactory))
    }
}
