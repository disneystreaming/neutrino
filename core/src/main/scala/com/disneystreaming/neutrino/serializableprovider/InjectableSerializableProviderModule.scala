package com.disneystreaming.neutrino.serializableprovider

import com.google.common.base.Preconditions
import com.google.inject.{Binder, Key}
import com.disneystreaming.neutrino.lang.JAnnotation
import com.disneystreaming.neutrino._
import com.disneystreaming.neutrino.annotation.Mark
import com.disneystreaming.neutrino.{ScalaPrivateModule, SingletonScope}
import net.codingwell.scalaguice.{InternalModule, typeLiteral}

import scala.reflect.runtime.universe._
import scala.reflect.{ClassTag, classTag}


trait InjectableSerializableProviderModule[B <: Binder] { module: InternalModule[B] =>
    /**
     * bind SerializableProvider[T] with the specified key.
     * The instance of SerializableProvider[T]  can be created
     * by calling {{{ injector.instance[SerializableProvider[T]](key.getAnnotation) }}}
     * or {{{ injector.instance[SerializableProvider[T]](key.getAnnotationType) }}}
     * <br/>Example:
     * {{{
     *     bind(key).to[TImpl].in[Scope]
     *     bindSerializableProvider(key)
     * }}}
     * <br/> How to expose the binding in private module:
     * {{{
     *     expose[SerializableProvider[T]].annotatedWith(key.getAnnotationType)
     *     // or
     *     expose[SerializableProvider[T]].annotatedWith(key.getAnnotation)
     * }}}
     * @param key The binding key
     * @tparam T The binding type
     */
    protected def bindSerializableProvider[T: TypeTag](key: Key[T]): Unit = {
        Preconditions.checkNotNull(key)

        module.binderAccess.install(new ScalaPrivateModule { inner =>
            override def configure(): Unit = {
                inner.bind[T].annotatedWith(classOf[Mark]).to(key)

                inner.bind[InjectableSerializableProviderProvider[T]].in[SingletonScope]
                inner.bind[SerializableProvider[T]]
                    .toProvider[InjectableSerializableProviderProvider[T]].in[SingletonScope]
                if (key.hasAnnotation) {
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

    /**
     * bind SerializableProvider[T] with the specified type and annotation instance.
     * The instance of SerializableProvider[T]  can be created
     * by calling {{{ injector.instance[SerializableProvider[T]](annotation) }}}
     * <br/>Example:
     * {{{
     *     bind[T].annotatedWith(annotation).to[TImpl].in[Scope]
     *     bindSerializableProvider[T](annotation)
     * }}}
     * <br/> How to expose the binding in private module:
     * {{{
     *     expose[SerializableProvider[T]].annotatedWith(annotation)
     * }}}
     * @param annotation the annotation instance of the binding
     * @tparam T The binding type
     */
    protected def bindSerializableProvider[T: TypeTag](annotation: JAnnotation): Unit = {
        bindSerializableProvider[T](Key.get(typeLiteral[T], annotation))
    }

    /**
     * bind SerializableProvider[T] with the specified type.
     * The instance of SerializableProvider[T] can be created
     * by calling {{{ injector.instance[SerializableProvider[T]] }}}
     * <br/>Example:
     * {{{
     *     bind[T].to[TImpl].in[Scope]
     *     bindSerializableProvider[T]()
     * }}}
     * <br/> How to expose the binding in private module:
     * {{{
     *     expose[SerializableProvider[T]]
     * }}}
     * @tparam T The binding type
     */
    protected def bindSerializableProvider[T: TypeTag](): Unit = {
        bindSerializableProvider[T](Key.get(typeLiteral[T]))
    }

    /**
     * bind SerializableProvider[T] with the specified type and annotation type.
     * The instance of SerializableProvider[T] can be created
     * by calling {{{ injector.instance[SerializableProvider[T], TAnnotation]() }}}
     * <br/>Example:
     * {{{
     *     bind[T].annotatedWith[TAnnotation].to[TImpl].in[Scope]
     *     bindSerializableProvider[T, TAnnotation]()
     * }}}
     * <br/> How to expose the binding in private module:
     * {{{
     *     expose[SerializableProvider[T]].annotatedWith[TAnnotation]
     * }}}
     * @tparam T The binding type
     * @tparam TAnnotation The binding annotation type
     */
    protected def bindSerializableProvider[T: TypeTag, TAnnotation <: JAnnotation : ClassTag](): Unit = {
        bindSerializableProvider[T](Key.get(typeLiteral[T], classTag[TAnnotation].runtimeClass.asInstanceOf[Class[JAnnotation]]))
    }
}
