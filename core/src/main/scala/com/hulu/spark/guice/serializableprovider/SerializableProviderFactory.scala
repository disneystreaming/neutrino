package com.hulu.spark.guice.serializableprovider

import com.google.inject.Provider

trait SerializableProviderFactory {
    def getSerializableProvider[T](rawProvider: Provider[T]): SerializableProvider[T]
}
