package com.hulu.neutrino

import com.google.inject.Binder

import scala.collection.mutable
import scala.reflect.ClassTag
import scala.reflect.macros.whitebox

// scalastyle:off
object TypeInfoAutoBindingMacro {
    def bindWithSingleTypeInfo[T: c.WeakTypeTag, TScope: c.WeakTypeTag](c: whitebox.Context)(): c.Expr[Unit] = {
        import c.universe._
        bindWithTypeInfoInternal(c)(
            c.Expr[Binder => Unit](
                q"""{(b:com.google.inject.Binder) =>
                    b.bindType[${c.weakTypeOf[T]}]
                        .inType[${c.weakTypeOf[TScope]}]()
                    }
                """))(c.weakTypeTag[T], c.weakTypeTag[T])
    }

    def bindWithTypeInfo[TFrom: c.WeakTypeTag, TTo: c.WeakTypeTag, TScope: c.WeakTypeTag]
    (c: whitebox.Context)(): c.Expr[Unit] = {
        import c.universe._
        bindWithTypeInfoInternal(c)(
            c.Expr[Binder => Unit](
                q"""{(b:com.google.inject.Binder) =>
                    b.bindType[${c.weakTypeOf[TFrom]}]
                        .toType[${c.weakTypeOf[TTo]}]
                        .inType[${c.weakTypeOf[TScope]}]()
                    }
                """))(c.weakTypeTag[TFrom], c.weakTypeTag[TTo])
    }

    def bindProviderWithTypeInfo[TFrom: c.WeakTypeTag, TProvider: c.WeakTypeTag, TScope: c.WeakTypeTag]
    (c: whitebox.Context)(): c.Expr[Unit] = {
        import c.universe._
        bindWithTypeInfoInternal(c)(
            c.Expr[Binder => Unit](
                q"""{(b:com.google.inject.Binder) =>
                    b.bindType[${c.weakTypeOf[TFrom]}]
                        .toProviderTypeTag[${c.weakTypeOf[TProvider]}]
                        .inType[${c.weakTypeOf[TScope]}]()
                    }
                """))(c.weakTypeTag[TFrom], c.weakTypeTag[TProvider])
    }

    def bindWithTypeInfoInternal[TFrom: c.WeakTypeTag, TTo: c.WeakTypeTag](c: whitebox.Context)
                                                                          (bindingFunc: c.Expr[Binder => Unit]): c.Expr[Unit] = {
        import c.universe._

        var classTagSet = mutable.HashSet[c.Type]()
        var manifestSet = mutable.HashSet[c.Type]()
        var typeTagSet = mutable.HashSet[c.Type]()

        val typeFrom = c.weakTypeOf[TFrom]
        val typeTo = c.weakTypeOf[TTo]

        val from = typeTo.etaExpand.typeParams
        val to = typeTo.typeArgs

        def getConstructorParamTypes(ttag: c.Type): Seq[Symbol] = {
            ttag.decls.find(_.isConstructor).get.
                asMethod.paramLists.flatten
        }

        val paramTypes = getConstructorParamTypes(typeTo)
        paramTypes.foreach { symbol =>
            val ts = symbol.typeSignature
            val erasure = ts.erasure
            if (erasure.=:=(typeOf[ClassTag[_]].erasure)) {
                classTagSet += ts.substituteTypes(from, to).dealias
            } else if (erasure.=:=(typeOf[Manifest[_]].erasure)) {
                manifestSet += ts.substituteTypes(from, to).dealias
            } else if (erasure.=:=(typeOf[TypeTag[_]].erasure)) {
                typeTagSet += ts.substituteTypes(from, to).dealias
            }
        }

        c.Expr[Unit](
            if (classTagSet.nonEmpty || manifestSet.nonEmpty || typeTagSet.nonEmpty) {
                val treeSeq = classTagSet.map { ct =>
                    q"${TermName("inner")}.binderAccess.bindType[$ct].toInstance(com.hulu.reflect.Types.typeTagToClassTag(typeTag[${ct.typeArgs.head}]))"
                } ++ manifestSet.map { m =>
                    q"${TermName("inner")}.binderAccess.bindType[$m].toInstance(com.hulu.reflect.Types.typeTagToManifest(typeTag[${m.typeArgs.head}]))"
                } ++ typeTagSet.map { t =>
                    q"${TermName("inner")}.binderAccess.bindType[$t].toInstance(typeTag[${t.typeArgs.head}])"
                }

                q"""
                this.binderAccess.install(
                    new net.codingwell.scalaguice.ScalaPrivateModule { inner =>
                        import com.hulu.guice._
                        import net.codingwell.scalaguice.BindingExtensions._
                        override def configure(): Unit = {
                            ..${treeSeq}
                            ${bindingFunc}(inner.binderAccess)
                            inner.expose[$typeFrom]
                        }
                    }
                )
           """
            } else {
                q"${bindingFunc}(this.binderAccess)"
            }
        )
    }
}
