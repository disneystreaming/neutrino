package com.hulu.neutrino.serializableprovider

import com.google.inject.Provider

trait SerializableProviderFactory {
    def getSerializableProvider[T](rawProvider: Provider[T]): SerializableProvider[T]
}
