package com.hulu.spark.guice.binding

import com.hulu.guice.SingletonScope
import net.codingwell.scalaguice.ScalaModule.ScalaScopedBindingBuilder

trait ScopeExtension { self =>
    implicit class ScopeExtension(private val builder : ScalaScopedBindingBuilder) {
        def inSingleton(): Unit = {
            builder.in[SingletonScope]
        }
    }
}
