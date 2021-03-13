package com.hulu.neutrino.`macro`

import com.hulu.neutrino.lang.JSerializable

import java.io.Serializable
import scala.language.experimental.macros

object SerializableProxy {
    def createProxy[T](supplier: () => T): T with JSerializable =  macro SerializableProxyMacro.createProxy[T]
}

