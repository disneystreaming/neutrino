package com.disneystreaming

import com.google.inject.{Key, Module}
import com.disneystreaming.neutrino.injectorbuilder.{SparkEnvironmentHolder, SparkInjectorBuilder}
import com.disneystreaming.neutrino.lang.JSerializable
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

    private[neutrino] object SparkSessionExtensions {
        val builderNameMap = new mutable.WeakHashMap[SparkContext, mutable.Set[String]]()
    }

    implicit class KeyExtensions[T](private val key: Key[T]) extends AnyVal {
        def hasAnnotation: Boolean = {
            key.getAnnotation != null || key.getAnnotationType != null
        }
    }

    implicit class SparkSessionExtensions(private val sparkSession: SparkSession) extends AnyVal {

        /**
         * Create the [[InjectorBuilder]] for the give name.
         * <br/>Each [[InjectorBuilder]] is corresponding to its own dependency graph which may contain multiple [[SparkInjector]]s.
         * <br/>The name is the unique id to distinguish different graphs created on the same [[org.apache.spark.sql.SparkSession]].
         * <br/>This method is commonly used in unit test.
         * @param builderName The unique id to distinguish different graphs created on the same [[org.apache.spark.sql.SparkSession]],
         *             the default value is "&#95;&#95;default&#95;&#95;"
         * @return The [[InjectorBuilder]] instance for the given name
         */
        def newInjectorBuilder(builderName: String = "__default__"): InjectorBuilder = {
            if (!SparkSessionExtensions.builderNameMap.contains(sparkSession.sparkContext)) {
                SparkSessionExtensions.builderNameMap.put(sparkSession.sparkContext, mutable.Set())
            }

            if (SparkSessionExtensions.builderNameMap(sparkSession.sparkContext).contains(builderName)) {
                throw new RuntimeException(s"duplicate builder name $builderName for current sparkContext")
            } else {
                SparkSessionExtensions.builderNameMap(sparkSession.sparkContext).add(builderName)
            }

            new SparkInjectorBuilder(sparkSession, builderName)
        }

        /**
         * Create a [[InjectorBuilder]] for the give name and complete the building
         * @param builderName The unique id to distinguish different graphs created on the same [[org.apache.spark.sql.SparkSession]]
         * @param modules The module set to describe the bindings of the graph
         * @return The [[SparkInjector]] built from the module set
         */
        def newSingleInjector(builderName: String, modules: SerializableModule*): SparkInjector = {
            val builder = newInjectorBuilder(builderName)
            val injector = builder.newRootInjector(modules:_*)
            builder.completeBuilding()
            injector
        }

        /**
         * Create a [[InjectorBuilder]] for the give name and complete the building
         * @param modules The module set to describe the bindings of the graph
         * @return The [[SparkInjector]] built from the module set
         */
        def newSingleInjector(modules: SerializableModule*): SparkInjector = {
            val builder = newInjectorBuilder()
            val injector = builder.newRootInjector(modules:_*)
            builder.completeBuilding()
            injector
        }

        /**
         * Create the [[StreamingContext]] instance for the given batchDuration.
         * This method should be called in the called back of {@link #getOrCreateStreamingContext(String, SparkSession => StreamingContext)} method
         * @param batchDuration The spark streaming batch duration
         * @return The [[StreamingContext]] instance for the given batchDuration
         */
        def newStreamingContext(batchDuration: Duration): StreamingContext = {
            val streamingContext = new StreamingContext(sparkSession.sparkContext, batchDuration)
            SparkEnvironmentHolder.setStreamingContext(sparkSession.sparkContext, streamingContext)
            streamingContext
        }

        /**
         * Create the [[StreamingContext]] instance for the given batchDuration.
         * This method should be called in the called back of {@link #getOrCreateStreamingContext(String, SparkSession => StreamingContext)} method
         * @param batchDuration The spark streaming batch duration
         * @return The [[StreamingContext]] instance for the given batchDuration
         */
        def newStreamingContext(batchDuration: java.time.Duration): StreamingContext = {
            newStreamingContext(Duration(batchDuration.toMillis))
        }

        /**
         * Recover a [[StreamingContext]] instance from the checkpoint or create a new one.
         * This method must be called when using neutrino in checkpoint enabled spark jobs.
         * <br/>Example:
         * {{{
         *     //create the injector
         *     val injector = ...
         *
         *     val streamingContext = sparkSession.getOrCreateStreamingContext(checkpointPath, session => {
         *        // don't call the StreamingContext constructor directly
         *         val streamContext = session.newStreamingContext(Duration(1000*30))
         *        streamContext.checkpoint(checkpointPath)
         *        // logic here to build the DAG with the streamContext
         *
         *        streamContext
         *     })
         *
         *     streamingContext.start()
         *     streamingContext.awaitTermination()
         * }}}
         * @param checkpointPath The checkpoint path of the spark job
         * @param func The callback to create a [[StreamingContext]] instance
         * @return The created or recovered [[StreamingContext]] instance
         */
        def getOrCreateStreamingContext(checkpointPath: String, func: SparkSession => StreamingContext): StreamingContext = {
            val session = sparkSession
            val streamingContext = StreamingContext.getOrCreate(checkpointPath, () => func.apply(session))
            SparkEnvironmentHolder.setStreamingContext(sparkSession.sparkContext, streamingContext)
            streamingContext
        }
    }

    implicit class SparkInjectorExtensions(private val injector: SparkInjector) extends AnyVal {
        /**
         * Create an instance of type [[T]] from the injector
         * @tparam T instance type
         * @return The created instance of type [[T]]
         */
        def instance[T: TypeTag]: T = {
            injector.instanceByKey(typeLiteral[T].toKey)
        }

        /**
         * Create an instance with type [[T]] and an annotation from the injector
         * @param ann The binding annotation
         * @tparam T The binding type
         * @return The created instance
         */
        def instance[T: TypeTag](ann: Annotation): T =
            injector.instanceByKey(typeLiteral[T].annotatedWith(ann))

        /**
         * Create an instance with type [[T]] and annotation type [[Ann]] from the injector
         * @tparam T The binding type
         * @tparam Ann The binding annotation type
         * @return The created instance
         */
        def instance[T: TypeTag, Ann <: Annotation : ClassTag]: T =
            injector.instanceByKey(typeLiteral[T].annotatedWith[Ann])

        /**
         * Create an instance with type [[T]] and annotation type [[Ann]] from the injector
         * @param annotationType The binding annotation type
         * @tparam T The binding type
         * @tparam Ann The binding annotation type
         * @returnThe created instance
         */
        def instance[T: TypeTag, Ann <: Annotation](annotationType: Class[Ann]): T =
            injector.instanceByKey(typeLiteral[T].annotatedWith(annotationType))

        /**
         * Create an instance with type [[T]] and a [[com.google.inject.name.Named]] annotation from the injector.
         * It is same to call {@code instance[T](Names.named(name))}
         * @param name The [[com.google.inject.name.Named]] annotation's value
         * @tparam T The binding type
         * @return The created instance
         */
        def instanceWithName[T: TypeTag](name: String) : T =
            injector.instanceByKey(typeLiteral[T].annotatedWithName(name))
    }
}
