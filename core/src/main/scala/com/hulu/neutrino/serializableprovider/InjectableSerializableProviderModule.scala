package com.hulu.neutrino.serializableprovider

import com.google.common.base.Preconditions
import com.google.inject.{Binder, Key}
import com.hulu.neutrino.lang.JAnnotation
import com.hulu.neutrino.{ScalaPrivateModule, SingletonScope}
import net.codingwell.scalaguice.{InternalModule, typeLiteral}

import scala.reflect.runtime.universe._
import scala.reflect.{ClassTag, classTag}


trait InjectableSerializableProviderModule[B <: Binder] { module: InternalModule[B] =>
    protected def bindSerializableProvider[T: TypeTag](key: Key[T]): Unit = {
        Preconditions.checkNotNull(key)

        module.binderAccess.install(new ScalaPrivateModule { inner =>
            override def configure(): Unit = {
                inner.bind[T].to(key)
                inner.bind[InjectableSerializableProviderProvider[T]].in[SingletonScope]
                inner.bind[SerializableProvider[T]]
                    .toProvider[InjectableSerializableProviderProvider[T]].in[SingletonScope]
                if (key.getAnnotation != null || key.getAnnotationType != null) {
                    val providerTypeLiteral = typeLiteral[SerializableProvider[T]]
                    val replacedKey = key.ofType(providerTypeLiteral)
                    inner.bind(replacedKey)
                        .to(Key.get(providerTypeLiteral)).in(classOf[SingletonScope])
                    inner.expose(replacedKey)
                } else {
                    inner.expose[SerializableProvider[T]]
                }
            }
        })
    }

    protected def bindSerializableProvider[T: TypeTag](annotation: JAnnotation): Unit = {
        bindSerializableProvider[T](Key.get(typeLiteral[T], annotation))
    }

    protected def bindSerializableProvider[T: TypeTag](): Unit = {
        bindSerializableProvider[T](Key.get(typeLiteral[T]))
    }

    protected def bindSerializableProvider[T: TypeTag, TAnnotation <: JAnnotation : ClassTag](): Unit = {
        bindSerializableProvider[T](Key.get(typeLiteral[T], classTag[TAnnotation].runtimeClass.asInstanceOf[Class[JAnnotation]]))
    }
}
