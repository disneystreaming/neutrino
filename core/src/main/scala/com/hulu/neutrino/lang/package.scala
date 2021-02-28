package com.hulu.neutrino

package object lang {
    type Supplier[T] = () => T
    type JSupplier[T] = java.util.function.Supplier[T]
    type Consumer[T] = T => Unit
    type JConsumer[T] = java.util.function.Consumer[T]

    type JSerializable = java.io.Serializable

    type JAnnotation = java.lang.annotation.Annotation
    type JType = java.lang.reflect.Type
}
