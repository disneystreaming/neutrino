package com.hulu.spark.guice.injectorbuilder

import com.google.common.base.Preconditions
import com.google.inject.Provider
import com.hulu.spark.guice.modulegraph.{GraphProperties, ModuleGraphProvider}
import com.hulu.spark.guice.serializableprovider.{SerializableProvider, SerializableProviderFactory}

import scala.collection.mutable

object SerializableProviderFactory {
    private[guice] final val PROVIDER_MAP = "PROVIDER_MAP"

    class SerializableProviderImpl[T](private val moduleGraphProvider: ModuleGraphProvider, private val index: Int)
        extends SerializableProvider[T] {
        override def get(): T = {
            moduleGraphProvider.moduleGraph
                .property[mutable.Map[Int, Provider[_]]](PROVIDER_MAP)
                .get(index).get().asInstanceOf[T]
        }
    }


    class IndexIncrementedSerializableProviderFactory(graphProperties: GraphProperties, moduleGraphProvider: ModuleGraphProvider)
        extends SerializableProviderFactory {
        Preconditions.checkNotNull(graphProperties)
        Preconditions.checkNotNull(moduleGraphProvider)

        private var index = 0

        override def getSerializableProvider[T](rawProvider: Provider[T]): SerializableProvider[T] = {
            val providerIndex = index
            providerMap.put(providerIndex, rawProvider)
            index += 1
            new SerializableProviderImpl(moduleGraphProvider, providerIndex)
        }

        private def providerMap: mutable.Map[Int, Provider[_]] = {
            if (graphProperties.getProperty(PROVIDER_MAP).isEmpty) {
                graphProperties.setProperty(PROVIDER_MAP, mutable.Map[Int, Provider[_]]())
            }

            graphProperties.getProperty(PROVIDER_MAP).get
        }
    }

}
