package com.hulu.spark.guice.serializablewrapper

import com.google.inject.Key
import com.hulu.guice.annotation.Wrapper
import com.hulu.guice.{SingletonScope, typeLiteral}
import com.hulu.spark.guice.SparkPrivateModule
import com.hulu.spark.guice.serializableprovider.SerializableProvider
import com.typesafe.scalalogging.StrictLogging

import scala.reflect.runtime.universe._

class InnerPrivateModule[IA : TypeTag](nestedKey: Key[IA], actualKey: Key[IA], func: SerializableProvider[IA] => IA)
    extends SparkPrivateModule with StrictLogging {
    override def configure(): Unit = {
        logger.info(s"nestedKey: ${nestedKey} actualKey: ${actualKey}")

        bind[IA].annotatedWith[Wrapper].to(nestedKey)
        bind[SerializableProvider[IA] => IA].annotatedWith[Wrapper].toInstance(func)
        bind[SerializableWrapperProvider[IA]].inSingleton()
        bind(actualKey).toProvider(typeLiteral[SerializableWrapperProvider[IA]]).in(classOf[SingletonScope])
        // expose the actual binding
        expose(actualKey)
    }
}
