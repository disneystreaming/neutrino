package com.disneystreaming.neutrino

import com.google.inject.Provider
import com.disneystreaming.neutrino.lang.JSerializable

/**
 * A provider interface which is serializable,
 * which is usually used to encapsulate the logic to create instances from the graph in spark jobs
 * @tparam T the instance type
 */
trait SerializableProvider[T] extends Provider[T] with JSerializable
