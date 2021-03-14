package com.hulu.neutrino.serializableprovider

import com.google.inject.MembersInjector

import java.lang.reflect.Method

private[neutrino] class SetterMethodMembersInjector[T](setterMethod: Method, parameter: Object) extends MembersInjector[T] {
    override def injectMembers(instance: T): Unit = {
        setterMethod.invoke(instance, parameter)
    }
}
