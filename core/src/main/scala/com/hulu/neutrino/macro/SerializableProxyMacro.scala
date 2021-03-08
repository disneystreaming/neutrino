package com.hulu.neutrino.`macro`

import java.io.Serializable
import scala.reflect.macros.whitebox

// scalastyle:off
object SerializableProxyMacro {
    def createProxy[T: c.WeakTypeTag](c: whitebox.Context)(supplier: c.Expr[() => T]): c.Expr[T with Serializable] = {
        import c.universe._

        val weakTypeTag = c.weakTypeOf[T]
        val traitName = weakTypeTag.typeSymbol.fullName

        val supplierName = "supplier"
        val baseClasses = weakTypeTag.baseClasses.filter(s => !s.equals(weakTypeTag.typeSymbol) && s.isAbstract)
        val thisFrom = weakTypeTag.etaExpand.typeParams
        val thisTo = weakTypeTag.typeArgs
        val from = baseClasses
            .flatMap{ baseSymbol =>
                weakTypeTag.baseType(baseSymbol).etaExpand.typeParams
            } ++ thisFrom

        val to = baseClasses
            .flatMap{ baseSymbol =>
                weakTypeTag.baseType(baseSymbol).typeArgs
            } ++ thisTo

        val newMethods = weakTypeTag.members.filter(_.isAbstract)
            .map { methodToAdd =>
                val methodSymbol = methodToAdd.asMethod
                val formalParams = methodSymbol.paramLists.map(_.map {
                    paramSymbol => ValDef(
                        Modifiers(Flag.PARAM, typeNames.EMPTY, List()),
                        paramSymbol.name.toTermName,
                        TypeTree(paramSymbol.typeSignature.substituteTypes(from, to)), // replace generic types to actual ones
                        EmptyTree)
                })

                // This AST node corresponds to the following Scala code:
                // supplierName.apply()
                val actual = Apply(Select(Ident(TermName(supplierName)), TermName("apply")), List())
                val result = if (methodSymbol.paramLists.nonEmpty) {
                    // actual.methodSymbol(params)
                    var tempResult = Apply(Select(actual, methodSymbol.name),
                        methodSymbol.paramLists.head.map(param => Ident(param.name)))
                    // for method which has multiple parameter list
                    methodSymbol.paramLists.slice(1, methodSymbol.paramLists.size).foreach { paramList =>
                        tempResult = Apply(tempResult, paramList.map(param => Ident(param.name)))
                    }

                    tempResult
                } else {
                    // for method which take no parameters
                    // This AST node corresponds to the following Scala code:
                    // actual.methodName
                    Select(actual, methodSymbol.name)
                }


                DefDef(Modifiers(Flag.OVERRIDE),
                    methodSymbol.name,
                    methodSymbol.typeParams.map{t =>
                        c.internal.typeDef(t)
                    },
                    formalParams,
                    TypeTree(methodSymbol.returnType.substituteTypes(from, to)),
                    result)
        }


        val newClassName = s"${traitName.replace('.', '_')}_with_serializable"
        val body = newMethods.toList
        c.Expr[T with Serializable](q"""
           class ${TypeName(newClassName)}(private var ${TermName(supplierName)}: () => ${c.weakTypeOf[T]})
           extends ${c.weakTypeOf[T]}
                with java.io.Serializable with com.esotericsoftware.kryo.KryoSerializable {
                ..${body}

                @throws[java.io.IOException]
                private def writeObject(out: java.io.ObjectOutputStream): Unit = {
                    out.writeObject(${TermName(supplierName)})
                }

                @throws[java.io.IOException]
                @throws[ClassNotFoundException]
                private def readObject(in: java.io.ObjectInputStream): Unit = {
                    ${TermName(supplierName)} = in.readObject().asInstanceOf[() => ${c.weakTypeOf[T]}]
                }

                override def write(kryo: com.esotericsoftware.kryo.Kryo, output: com.esotericsoftware.kryo.io.Output): Unit = {
                    kryo.writeObject(output, ${TermName(supplierName)})
                }


                override def read(kryo: com.esotericsoftware.kryo.Kryo, input: com.esotericsoftware.kryo.io.Input): Unit = {
                    ${TermName(supplierName)} = kryo.readClassAndObject(input).asInstanceOf[() => ${c.weakTypeOf[T]}]
                }
           }
           new ${TypeName(newClassName)}($supplier)
         """)
    }
}
// scalastyle:on