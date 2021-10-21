package com.disneystreaming.neutrino.serializableprovider

import com.disneystreaming.neutrino.SerializableProvider
import com.disneystreaming.neutrino.annotation.{InjectSerializableProvider, Mark}

import javax.inject.{Inject, Provider}

private[neutrino] class InjectableSerializableProviderProvider[T] @Inject() () extends Provider[SerializableProvider[T]] {

    private var provider: SerializableProvider[T] = _

    @InjectSerializableProvider
    def setSerializableProvider(@Mark provider: SerializableProvider[T]): Unit = {
        this.provider = provider
    }

    override def get(): SerializableProvider[T] = {
        this.provider
    }
}
