package com.hulu.neutrino.serializableprovider

import com.hulu.neutrino.annotation.InjectSerializableProvider

import javax.inject.{Inject, Provider}

class InjectableSerializableProviderProvider[T] @Inject() () extends Provider[SerializableProvider[T]] {

    private var provider: SerializableProvider[T] = _

    @InjectSerializableProvider
    def setSerializableProvider(provider: SerializableProvider[T]): Unit = {
        this.provider = provider
    }

    override def get(): SerializableProvider[T] = {
        this.provider
    }
}
