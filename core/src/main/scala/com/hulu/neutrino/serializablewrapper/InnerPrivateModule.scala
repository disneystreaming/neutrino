package com.hulu.neutrino.serializablewrapper

import com.google.inject.Key
import com.hulu.neutrino.{SingletonScope, SparkPrivateModule}
import com.hulu.neutrino.annotation.Wrapper
import com.hulu.neutrino.serializableprovider.SerializableProvider
import com.typesafe.scalalogging.StrictLogging
import net.codingwell.scalaguice.typeLiteral

import scala.reflect.runtime.universe._

class InnerPrivateModule[IA : TypeTag](nestedKey: Key[IA], actualKey: Key[IA], func: SerializableProvider[IA] => IA)
    extends SparkPrivateModule with StrictLogging {
    override def configure(): Unit = {
        logger.info(s"nestedKey: ${nestedKey} actualKey: ${actualKey}")

        bind[IA].annotatedWith[Wrapper].to(nestedKey)
        bind[SerializableProvider[IA] => IA].annotatedWith[Wrapper].toInstance(func)
        bind[SerializableWrapperProvider[IA]].in[SingletonScope]
        bind(actualKey).toProvider(typeLiteral[SerializableWrapperProvider[IA]]).in(classOf[SingletonScope])
        // expose the actual binding
        expose(actualKey)
    }
}
