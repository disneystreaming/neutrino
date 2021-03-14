package com.hulu.neutrino.serializableprovider

import com.google.inject.Provider
import com.hulu.neutrino.SerializableProvider

private[neutrino] trait SerializableProviderFactory {
    def getSerializableProvider[T](rawProvider: Provider[T]): SerializableProvider[T]
}
