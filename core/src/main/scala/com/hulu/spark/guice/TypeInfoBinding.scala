package com.hulu.spark.guice

import com.google.inject.Binder
import com.hulu.spark.Types
import net.codingwell.scalaguice.InternalModule

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

trait TypeInfoBinding[B <: Binder] { self : InternalModule[B] =>
    def bindClassTag[T : TypeTag]: Unit = {
        self.bind[ClassTag[T]].toInstance(Types.typeTagToClassTag(typeTag[T]))
    }

    def bindManifest[T : TypeTag]: Unit = {
        self.bind[Manifest[T]].toInstance(Types.typeTagToManifest(typeTag[T]))
    }

    def bindTypeTag[T : TypeTag]: Unit = {
        self.bind[TypeTag[T]].toInstance(typeTag[T])
    }
}
