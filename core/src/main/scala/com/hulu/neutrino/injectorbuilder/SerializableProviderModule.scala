package com.hulu.neutrino.injectorbuilder

import com.google.inject.matcher.Matchers
import com.hulu.neutrino.lang.JSerializable
import com.hulu.neutrino.{ScalaModule, SerializableModule}
import com.hulu.neutrino.serializableprovider.{SerializableProviderFactory, SerializableProviderTypeListener}

class SerializableProviderModule(serializableProviderFactory: SerializableProviderFactory)
    extends ScalaModule with JSerializable {
    override def configure(): Unit = {
        bindListener(Matchers.any, new SerializableProviderTypeListener(serializableProviderFactory))
    }
}
