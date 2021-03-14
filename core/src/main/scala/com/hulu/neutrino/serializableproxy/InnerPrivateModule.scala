package com.hulu.neutrino.serializableproxy

import com.google.inject.Key
import com.hulu.neutrino.annotation.Mark
import com.hulu.neutrino.{SerializableProvider, SingletonScope, SparkPrivateModule}
import com.typesafe.scalalogging.StrictLogging
import net.codingwell.scalaguice.typeLiteral

import scala.reflect.runtime.universe._

class InnerPrivateModule[IA : TypeTag](nestedKey: Key[IA], actualKey: Key[IA], func: SerializableProvider[IA] => IA)
    extends SparkPrivateModule with StrictLogging {
    override def configure(): Unit = {
        logger.info(s"nestedKey: ${nestedKey} actualKey: ${actualKey}")

        bind[IA].annotatedWith[Mark].to(nestedKey)
        bind[SerializableProvider[IA] => IA].annotatedWith[Mark].toInstance(func)
        bind[SerializableProxyProvider[IA]].in[SingletonScope]
        bind(actualKey).toProvider(typeLiteral[SerializableProxyProvider[IA]]).in(classOf[SingletonScope])
        // expose the actual binding
        expose(actualKey)
    }
}
