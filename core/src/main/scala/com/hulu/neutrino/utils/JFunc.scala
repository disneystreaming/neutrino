package com.hulu.neutrino.utils

import java.util.function.{Function => jFunction}

object JFunc {
    def apply[TInput, TReturn](scalaFunc: TInput => TReturn): jFunction[TInput, TReturn] = {
        new jFunction[TInput, TReturn] {
            override def apply(t: TInput): TReturn = {
                scalaFunc.apply(t)
            }
        }
    }
}
