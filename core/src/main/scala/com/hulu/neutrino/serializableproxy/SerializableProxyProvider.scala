package com.hulu.neutrino.serializableproxy

import com.google.inject.Provider
import com.hulu.neutrino.SerializableProvider
import com.hulu.neutrino.annotation.{InjectSerializableProvider, Mark}

import javax.inject.Inject

private[neutrino] class SerializableProxyProvider[T] @Inject()(@Mark func: SerializableProvider[T] => T) extends Provider[T] {
    private var serializableProvider: SerializableProvider[T] = _

    @InjectSerializableProvider
    def setProvider(@Mark serializableProvider: SerializableProvider[T]): Unit = {
        this.serializableProvider = serializableProvider
    }

    override def get(): T = {
        func(serializableProvider)
    }
}
