package com.hulu.neutrino.serializablewrapper

import com.google.inject.Provider
import com.hulu.neutrino.annotation.{InjectSerializableProvider, Mark}
import com.hulu.neutrino.serializableprovider.SerializableProvider

import javax.inject.Inject

class SerializableWrapperProvider[T] @Inject() (@Mark func: SerializableProvider[T] => T) extends Provider[T] {
    private var serializableProvider: SerializableProvider[T] = _

    @InjectSerializableProvider
    def setProvider(@Mark serializableProvider: SerializableProvider[T]): Unit = {
        this.serializableProvider = serializableProvider
    }

    override def get(): T = {
        func(serializableProvider)
    }
}
