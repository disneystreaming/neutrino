package com.disneystreaming.neutrino.serializableprovider

import com.disneystreaming.neutrino.SerializableProvider
import com.google.inject.Provider
import com.disneystreaming.neutrino.SerializableProvider

private[neutrino] trait SerializableProviderFactory {
    def getSerializableProvider[T](rawProvider: Provider[T]): SerializableProvider[T]
}
