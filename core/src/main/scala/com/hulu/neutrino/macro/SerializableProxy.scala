package com.hulu.neutrino.`macro`

import java.io.Serializable
import scala.language.experimental.macros

object SerializableProxy {
    def createProxy[T](supplier: () => T): T with Serializable =  macro SerializableProxyMacro.createProxy[T]
}

