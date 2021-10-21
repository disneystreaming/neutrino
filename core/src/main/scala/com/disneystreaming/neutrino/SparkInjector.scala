package com.disneystreaming.neutrino

import com.google.inject.Key
import com.disneystreaming.neutrino.lang.JSerializable

trait SparkInjector extends JSerializable {
    def instanceByKey[T](key: Key[T]): T

    def createChildInjector(modules: SerializableModule*): SparkInjector
}
