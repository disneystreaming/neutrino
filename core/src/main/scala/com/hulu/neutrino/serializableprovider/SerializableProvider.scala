package com.hulu.neutrino.serializableprovider

import com.google.inject.Provider
import com.hulu.neutrino.lang.JSerializable

trait SerializableProvider[T] extends Provider[T] with JSerializable
