package com.hulu.spark.guice.serializableprovider

import com.google.inject.Provider
import com.hulu.spark.JSerializable

trait SerializableProvider[T] extends Provider[T] with JSerializable
