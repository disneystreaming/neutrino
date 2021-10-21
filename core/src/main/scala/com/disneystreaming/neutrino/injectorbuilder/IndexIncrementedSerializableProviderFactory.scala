package com.disneystreaming.neutrino.injectorbuilder

import com.disneystreaming.neutrino.SerializableProvider
import com.disneystreaming.neutrino.graph.GraphProperties
import com.google.common.base.Preconditions
import com.google.inject.Provider
import com.disneystreaming.neutrino.SerializableProvider
import com.disneystreaming.neutrino.graph.{GraphProperties, ModuleGraphProvider}
import com.disneystreaming.neutrino.serializableprovider.SerializableProviderFactory

import scala.collection.mutable

private[neutrino] object IndexIncrementedSerializableProviderFactory {
    final val PROVIDER_MAP = "PROVIDER_MAP"

    class SerializableProviderImpl[T](private val moduleGraphProvider: ModuleGraphProvider, private val index: Int)
        extends SerializableProvider[T] {
        override def get(): T = {
            moduleGraphProvider.moduleGraph
                .property[mutable.Map[Int, Provider[_]]](PROVIDER_MAP)
                .get(index).get().asInstanceOf[T]
        }
    }
}

private[neutrino] class IndexIncrementedSerializableProviderFactory(
    graphProperties: GraphProperties, moduleGraphProvider: ModuleGraphProvider)
    extends SerializableProviderFactory {
    Preconditions.checkNotNull(graphProperties)
    Preconditions.checkNotNull(moduleGraphProvider)

    private var index = 0

    override def getSerializableProvider[T](rawProvider: Provider[T]): SerializableProvider[T] = {
        val providerIndex = index
        providerMap.put(providerIndex, rawProvider)
        index += 1
        new IndexIncrementedSerializableProviderFactory.SerializableProviderImpl(moduleGraphProvider, providerIndex)
    }

    private def providerMap: mutable.Map[Int, Provider[_]] = {
        if (graphProperties.getProperty(IndexIncrementedSerializableProviderFactory.PROVIDER_MAP).isEmpty) {
            graphProperties.setProperty(IndexIncrementedSerializableProviderFactory.PROVIDER_MAP, mutable.Map[Int, Provider[_]]())
        }

        graphProperties.getProperty(IndexIncrementedSerializableProviderFactory.PROVIDER_MAP).get
    }
}
