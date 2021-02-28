package com.hulu

import com.google.common.base.Joiner
import org.apache.spark.sql.SparkSession

import _root_.scala.collection.JavaConverters._
import _root_.scala.reflect.ClassTag

package object spark {
    type Supplier[T] = () => T
    type JSupplier[T] = java.util.function.Supplier[T]
    type Consumer[T] = T => Unit
    type JConsumer[T] = java.util.function.Consumer[T]

    type JSerializable = java.io.Serializable

    type JAnnotation = java.lang.annotation.Annotation

    type JInt = java.lang.Integer
    type JInteger = java.lang.Integer
    type JDouble = java.lang.Double
    type JFloat = java.lang.Float
    type JLong = java.lang.Long
    type JBoolean = java.lang.Boolean

    type JType = java.lang.reflect.Type

    //java collection types
    type JMap[K, V] = java.util.Map[K, V]
    type JSet[E] = java.util.Set[E]
    type JList[E] = java.util.List[E]


    implicit class SparkSessionBuilderExtensions(private val builder: SparkSession.Builder) extends AnyVal {
        def useKryoSerializer(registrators: Class[_]*): SparkSession.Builder = {
            builder.useSerializer[org.apache.spark.serializer.KryoSerializer]()
                    .config("spark.kryo.registrator", Joiner.on(',').join(registrators.map(_.getName).asJava))

            builder
        }

        def requireKryoRegistration(required: Boolean = true): SparkSession.Builder = {
            if (required) {
                builder.config("spark.kryo.registrationRequired", "true")
            }

            builder
        }

        def useSerializer[TSerializer <: org.apache.spark.serializer.Serializer]()(implicit ct: ClassTag[TSerializer]): SparkSession.Builder = {
            builder.config("spark.serializer", ct.runtimeClass.getName)
        }
    }
}
