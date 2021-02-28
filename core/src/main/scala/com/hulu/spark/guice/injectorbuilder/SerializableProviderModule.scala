package com.hulu.spark.guice.injectorbuilder

import com.google.inject.matcher.Matchers
import com.hulu.spark.guice.serializableprovider.{SerializableProviderFactory, SerializableProviderTypeListener}
import com.hulu.spark.guice.{ScalaModule, SerializableModule}

class SerializableProviderModule(serializableProviderFactory: SerializableProviderFactory)
    extends ScalaModule with SerializableModule {
    override def configure(): Unit = {
        bindListener(Matchers.any, new SerializableProviderTypeListener(serializableProviderFactory))
    }
}
