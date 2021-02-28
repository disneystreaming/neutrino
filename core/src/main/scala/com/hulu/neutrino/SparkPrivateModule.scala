package com.hulu.neutrino

import com.google.inject.{PrivateBinder, PrivateModule}
import com.hulu.neutrino.serializablewrapper.SerializableWrapperModule

abstract class SparkPrivateModule
    extends PrivateModule
        with ScalaPrivateModule
        with SerializableModule
        with SerializableWrapperModule[PrivateBinder]
        with TypeInfoAutoBinding[PrivateBinder]
        with TypeInfoBinding[PrivateBinder]
