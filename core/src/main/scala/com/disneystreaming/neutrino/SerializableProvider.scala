package com.disneystreaming.neutrino

import com.google.inject.Provider
import com.disneystreaming.neutrino.lang.JSerializable

trait SerializableProvider[T] extends Provider[T] with JSerializable
