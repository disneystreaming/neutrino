package com.hulu.neutrino

import com.hulu.neutrino.lang.JType
import com.hulu.neutrino.utils.JFunc

import java.util.concurrent.ConcurrentHashMap
import scala.reflect.{ClassTag, ManifestFactory}
import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.universe._

object Types {
    def javaTypeToTypeTag[T](tpe: JType, mirror: reflect.api.Mirror[ru.type]): TypeTag[T] = {
        TypeTag(mirror, new reflect.api.TypeCreator {
            def apply[U <: reflect.api.Universe with Singleton](m: reflect.api.Mirror[U]) = {
                // assert(m eq mirror, s"TypeTag[$tpe] defined in $mirror cannot be migrated to $m.")
                tpe.asInstanceOf[U#Type]
            }
        })
    }

    def typeToTypeTag[T](tpe: ru.Type, mirror: reflect.api.Mirror[ru.type]): TypeTag[T] = {
        TypeTag(mirror, new reflect.api.TypeCreator {
            def apply[U <: reflect.api.Universe with Singleton](m: reflect.api.Mirror[U]) = {
                // assert(m eq mirror, s"TypeTag[$tpe] defined in $mirror cannot be migrated to $m.")
                tpe.asInstanceOf[U#Type]
            }
        })
    }

    def typeTagToManifest[T](ttag: TypeTag[T]): Manifest[T] = {
        val mirror = ttag.mirror

        def toManifestRec(t: Type): Manifest[T] = {
            val clazz = ClassTag[T](mirror.runtimeClass(t)).runtimeClass.asInstanceOf[Class[T]]
            if (t.typeArgs.length == 1) {
                val arg = toManifestRec(t.typeArgs.head)
                ManifestFactory.classType(clazz, arg)
            } else if (t.typeArgs.length > 1) {
                val args = t.typeArgs.map(x => toManifestRec(x))
                ManifestFactory.classType(clazz, args.head, args.tail: _*)
            } else {
                ManifestFactory.classType(clazz)
            }
        }

        toManifestRec(ttag.tpe)
    }

    private val typeTagToClassTag = new ConcurrentHashMap[TypeTag[_], ClassTag[_]]()

    def typeTagToClassTag[T](ttag: TypeTag[T]): ClassTag[T] = {
        typeTagToClassTag.computeIfAbsent(ttag, JFunc { tag: TypeTag[_] =>
            val mirror = tag.mirror
            ClassTag.apply[T](mirror.runtimeClass(tag.tpe))
        }).asInstanceOf[ClassTag[T]]
    }

    def javaTypeToTypeTag[T](tpe: JType): TypeTag[T] = {
        javaTypeToTypeTag(tpe, ru.runtimeMirror(getClass.getClassLoader))
    }

    def typeToTypeTag[T](tpe: ru.Type): TypeTag[T] = {
        typeToTypeTag(tpe, ru.runtimeMirror(getClass.getClassLoader))
    }

    def typeToClassTag[T](tpe: ru.Type): ClassTag[T] = {
        typeTagToClassTag(typeToTypeTag(tpe, ru.runtimeMirror(getClass.getClassLoader)))
    }

    def typeToManifest[T](tpe: ru.Type): Manifest[T] = {
        typeTagToManifest(typeToTypeTag(tpe, ru.runtimeMirror(getClass.getClassLoader)))
    }

    def javaTypeToClassTag[T](tpe: JType): ClassTag[T] = {
        typeTagToClassTag(javaTypeToTypeTag(tpe))
    }
}
