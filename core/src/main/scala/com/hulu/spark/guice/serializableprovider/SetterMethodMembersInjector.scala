package com.hulu.spark.guice.serializableprovider

import com.google.inject.MembersInjector

import java.lang.reflect.Method

class SetterMethodMembersInjector[T](setterMethod: Method, parameter: Object) extends MembersInjector[T] {
    override def injectMembers(instance: T): Unit = {
        setterMethod.invoke(instance, parameter)
    }
}
