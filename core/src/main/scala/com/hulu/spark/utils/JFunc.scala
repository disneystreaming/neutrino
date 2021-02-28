package com.hulu.spark.utils

import com.hulu.spark.{Consumer, Supplier}

import java.util.function.{BiConsumer, BiFunction, Consumer => jConsumer, Function => jFunction, Supplier => jSupplier}

object JFunc {
    def apply[TInput, TReturn](scalaFunc: TInput => TReturn): jFunction[TInput, TReturn] = {
        new jFunction[TInput, TReturn] {
            override def apply(t: TInput): TReturn = {
                scalaFunc.apply(t)
            }
        }
    }

    def apply[TReturn](scalaSupplier: Supplier[TReturn]): jSupplier[TReturn] = {
        new jSupplier[TReturn] {
            override def get(): TReturn = {
                scalaSupplier()
            }
        }
    }

    def apply[TInput](scalaSupplier: Consumer[TInput]): jConsumer[TInput] = {
        new jConsumer[TInput] {
            override def accept(t: TInput): Unit = {
                scalaSupplier.apply(t)
            }
        }
    }

    def apply[T, U](scalaSupplier: (T, U) => Unit): BiConsumer[T, U] = {
        new BiConsumer[T, U] {
            override def accept(t: T, u: U): Unit = {
                scalaSupplier.apply(t, u)
            }
        }
    }


    def apply[T, U, R](scalaFunc: (T, U) => R): BiFunction[T, U, R] = {
        new BiFunction[T, U, R] {
            override def apply(t: T, u: U): R = {
                scalaFunc(t, u)
            }
        }
    }
}
