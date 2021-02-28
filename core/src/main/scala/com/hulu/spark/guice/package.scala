package com.hulu.spark

import com.hulu.spark.guice.injectorbuilder.SparkInjectorBuilder

import java.lang.annotation.Annotation
import net.codingwell.scalaguice.KeyExtensions._
import net.codingwell.scalaguice._
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Duration, StreamingContext}

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

package object guice {
    implicit class SparkSessionExtensions(private val sparkSession: SparkSession) extends AnyVal {
        def newInjectorBuilder(name: String = "default"): SparkInjectorBuilder = {
            SparkEnvironmentHolder.setDriver()
            SparkEnvironmentHolder.setSparkSession(sparkSession)

            new SparkInjectorBuilder(sparkSession, name)
        }

        def newSingleInjector(modules: SparkModule*): SparkInjector = {
            val builder = newInjectorBuilder()
            val injector = builder.newRootInjector(modules:_*)
            builder.prepareInjectors()
            injector
        }

        def newStreamingContext(batchDuration: Duration): StreamingContext = {
            val streamingContext = new StreamingContext(sparkSession.sparkContext, batchDuration)
            SparkEnvironmentHolder.setStreamingContext(streamingContext)
            streamingContext
        }

        def newStreamingContext(batchDuration: java.time.Duration): StreamingContext = {
            newStreamingContext(Duration(batchDuration.toMillis))
        }

        def getOrCreateStreamingContext(checkpointPath: String, func: SparkSession => StreamingContext): StreamingContext = {
            val session = sparkSession
            val streamingContext = StreamingContext.getOrCreate(checkpointPath, () => func.apply(session))
            SparkEnvironmentHolder.setStreamingContext(streamingContext)
            streamingContext
        }
    }

    implicit class SparkInjectorExtensions(private val injector: SparkInjector) extends AnyVal {
        def instance[T: TypeTag]: T = {
            injector.instanceByKey(typeLiteral[T].toKey)
        }

        def instance[T: TypeTag](ann: Annotation): T = injector.instanceByKey(typeLiteral[T].annotatedWith(ann))
        def instance[T: TypeTag, Ann <: Annotation : ClassTag]: T = injector.instanceByKey(typeLiteral[T].annotatedWith[Ann])
        def instanceWithName[T: TypeTag](name: String) : T = injector.instanceByKey(typeLiteral[T].annotatedWithName(name))
    }
}
