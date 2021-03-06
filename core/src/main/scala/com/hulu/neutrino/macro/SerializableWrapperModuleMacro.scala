package com.hulu.neutrino.`macro`

import com.google.inject.Key
import com.google.inject.name.Names
import com.hulu.neutrino.lang.JAnnotation
import com.hulu.neutrino.annotation.{ConcreteNestedAnnotation, NestedAnnotation}
import net.codingwell.scalaguice.ScalaModule.ScalaLinkedBindingBuilder
import net.codingwell.scalaguice.typeLiteral

import scala.reflect.ClassTag
import scala.reflect.macros.whitebox
import scala.reflect.runtime.universe._

// scalastyle:off
object SerializableWrapperModuleMacro {
    def getNestedAnnotation[T](key: Key[T]): NestedAnnotation = {
        ConcreteNestedAnnotation.builder()
            .innerAnnotation(key.getAnnotation)
            .innerAnnotationType(key.getAnnotationType)
            .build()
    }

    def getKey[T: TypeTag]: Key[T] = {
        Key.get(typeLiteral[T])
    }

    def getKey[T: TypeTag](annotation: JAnnotation): Key[T] = {
        Key.get(typeLiteral[T], annotation)
    }

    def getKey[T: TypeTag](annotationClazz: Class[_ <: JAnnotation]): Key[T] = {
        Key.get(typeLiteral[T], annotationClazz)
    }

    def getKey[T: TypeTag](annotationClazz: ClassTag[_ <: JAnnotation]): Key[T] = {
        Key.get(typeLiteral[T], annotationClazz.runtimeClass.asInstanceOf[Class[_ <: JAnnotation]])
    }

    // handle annotatedWithName
    def getKey[T: TypeTag](nameValue: String): Key[T] = {
        Key.get(typeLiteral[T], Names.named(nameValue))
    }

    def withSerializableWrapperImpl[T: c.WeakTypeTag](c: whitebox.Context): c.Expr[ScalaLinkedBindingBuilder[T]] = {

        import c.universe._

        val weakTypeTag = c.weakTypeOf[T]
        val line = c.enclosingPosition.line

        val q"$conv($builder)" = c.prefix.tree
        println(s"builder: $builder")
        var node = builder
        while (node.children.nonEmpty) {
            node = node.children.head
        }

        val module = q"${node}"
        println(s"1 firstnode: ${module}")
        var keyExpr: c.Tree = null
        builder match {
            case q"$moduleBind($tt) $annotatedWith($at)" => {
                println(s"2 module: $moduleBind \n tt: $tt \n annotatedWith: $annotatedWith \n at: $at")
                keyExpr = q"com.hulu.neutrino.`macro`.SerializableWrapperModuleMacro.getKey[${weakTypeTag}]($at)"
            }
            case q"$moduleBind($at)" => {
                keyExpr = q"com.hulu.neutrino.`macro`.SerializableWrapperModuleMacro.getKey[${weakTypeTag}]($at)"
            }
        }
        val bindingMethod = TermName(s"innerBinding_$line")
        c.Expr[ScalaLinkedBindingBuilder[T]](q"""
         def ${bindingMethod}(
            key: com.google.inject.Key[${weakTypeTag}]): net.codingwell.scalaguice.ScalaModule.ScalaLinkedBindingBuilder[${weakTypeTag}] = {
            import com.hulu.neutrino._
            import net.codingwell.scalaguice._
            import scala.reflect.runtime.universe._
            import com.hulu.neutrino.serializableprovider.SerializableProvider
            import com.twitter.chill.ClosureCleaner

            val nestedAnnotation = com.hulu.neutrino.`macro`.SerializableWrapperModuleMacro.getNestedAnnotation(key)
            val nestedKey = com.google.inject.Key.get(typeLiteral[${weakTypeTag}], nestedAnnotation)
            val func: SerializableProvider[${weakTypeTag}] => ${weakTypeTag} = p => {
                val serializableProvider: () => ${weakTypeTag} = () => p.get()
                ClosureCleaner(serializableProvider)
                ${SerializableProxyMacro.createProxy[T](c)(c.Expr[() => T](q"serializableProvider"))}
            }

            install(new com.hulu.neutrino.serializablewrapper.InnerPrivateModule[${weakTypeTag}](nestedKey, key, func))

            $module.bind[$weakTypeTag].annotatedWith(nestedAnnotation)
         }
         ${bindingMethod}(${keyExpr})
         """)
    }
}
// scalastyle:on
