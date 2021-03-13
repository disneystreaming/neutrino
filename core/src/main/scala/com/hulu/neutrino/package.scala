package com.hulu

import com.google.inject.{Key, Module}
import com.hulu.neutrino.injectorbuilder.SparkInjectorBuilder
import com.hulu.neutrino.lang.JSerializable
import net.codingwell.scalaguice.KeyExtensions._
import net.codingwell.scalaguice.typeLiteral
import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Duration, StreamingContext}

import java.lang.annotation.Annotation
import scala.collection.mutable
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

package object neutrino {
    type SerializableModule = Module with JSerializable
    type SingletonScope = com.google.inject.Singleton

    object SparkSessionExtensions {
        val builderNameMap = new mutable.WeakHashMap[SparkContext, mutable.Set[String]]()
    }

    implicit class KeyExtensions[T](private val key: Key[T]) extends AnyVal {
        def hasAnnotation: Boolean = {
            key.getAnnotation != null || key.getAnnotationType != null
        }
    }

    implicit class SparkSessionExtensions(private val sparkSession: SparkSession) extends AnyVal {
        def newInjectorBuilder(name: String = "default"): SparkInjectorBuilder = {
            if (!SparkSessionExtensions.builderNameMap.contains(sparkSession.sparkContext)) {
                SparkSessionExtensions.builderNameMap.put(sparkSession.sparkContext, mutable.Set())
            }

            if (SparkSessionExtensions.builderNameMap(sparkSession.sparkContext).contains(name)) {
                throw new RuntimeException(s"duplicate builder name $name for current sparkContext")
            } else {
                SparkSessionExtensions.builderNameMap(sparkSession.sparkContext).add(name)
            }

            new SparkInjectorBuilder(sparkSession, name)
        }

        def newSingleInjector(builderName: String, modules: SerializableModule*): SparkInjector = {
            val builder = newInjectorBuilder(builderName)
            val injector = builder.newRootInjector(modules:_*)
            builder.prepareInjectors()
            injector
        }

        def newSingleInjector(modules: SerializableModule*): SparkInjector = {
            val builder = newInjectorBuilder()
            val injector = builder.newRootInjector(modules:_*)
            builder.prepareInjectors()
            injector
        }

        def newStreamingContext(batchDuration: Duration): StreamingContext = {
            val streamingContext = new StreamingContext(sparkSession.sparkContext, batchDuration)
            SparkEnvironmentHolder.setStreamingContext(sparkSession.sparkContext, streamingContext)
            streamingContext
        }

        def newStreamingContext(batchDuration: java.time.Duration): StreamingContext = {
            newStreamingContext(Duration(batchDuration.toMillis))
        }

        def getOrCreateStreamingContext(checkpointPath: String, func: SparkSession => StreamingContext): StreamingContext = {
            val session = sparkSession
            val streamingContext = StreamingContext.getOrCreate(checkpointPath, () => func.apply(session))
            SparkEnvironmentHolder.setStreamingContext(sparkSession.sparkContext, streamingContext)
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
