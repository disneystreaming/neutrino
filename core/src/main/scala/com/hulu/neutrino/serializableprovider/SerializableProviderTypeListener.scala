package com.hulu.neutrino.serializableprovider

import com.google.inject.spi.{TypeEncounter, TypeListener}
import com.google.inject.{Key, Provider, TypeLiteral}
import com.hulu.neutrino.annotation.InjectSerializableProvider

import java.lang.reflect.{Modifier, ParameterizedType}

object SerializableProviderTypeListener {
    def getKey[T](typeLiteral: TypeLiteral[T], method: java.lang.reflect.Method):Key[_] = {
        val injectedParameter = method.getParameters()(0)
        if (injectedParameter.getDeclaredAnnotations.length > 1) {
            throw new RuntimeException(s"the injected parameter should only contain at most 1 annotation")
        }

        if(injectedParameter.getType.isAssignableFrom(classOf[Provider[_]]) ||
            injectedParameter.getType.isAssignableFrom(classOf[javax.inject.Provider[_]]) ||
            injectedParameter.getType.isAssignableFrom(classOf[SerializableProvider[_]])) {
            val requestType = typeLiteral.getParameterTypes(method).get(0).getType.asInstanceOf[ParameterizedType].getActualTypeArguments()(0)
            if (injectedParameter.getDeclaredAnnotations.nonEmpty) {
                Key.get(requestType, injectedParameter.getDeclaredAnnotations()(0))
            } else {
                Key.get(requestType)
            }
        } else {
            throw new RuntimeException(s"unsupported injected type: ${injectedParameter.getType}")
        }
    }
}

class SerializableProviderTypeListener(serializableProviderFactory: SerializableProviderFactory) extends TypeListener {
    override def hear[T](typeLiteral: TypeLiteral[T], typeEncounter: TypeEncounter[T]): Unit = {
        var clazz = typeLiteral.getRawType
        while (clazz != null) {
            for (method <- clazz.getDeclaredMethods) {
                if (method.getAnnotation(classOf[InjectSerializableProvider]) != null) {
                    if (!method.getName.startsWith("set") || !Modifier.isPublic(method.getModifiers) || method.getParameterCount != 1) {
                        throw new RuntimeException(
                            s"The 'InjectSerializableProvider' should only be annotated on a public setter method with only one parameter")
                    }

                    val key = SerializableProviderTypeListener.getKey(typeLiteral, method)
                    val rawProvider = typeEncounter.getProvider(key)
                    val serializableProvider = serializableProviderFactory.getSerializableProvider(rawProvider)
                    typeEncounter.register(new SetterMethodMembersInjector[T](method, serializableProvider))
                }
            }

            clazz = clazz.getSuperclass
        }
    }
}
