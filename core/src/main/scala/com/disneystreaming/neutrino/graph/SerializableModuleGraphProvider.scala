package com.disneystreaming.neutrino.graph

import com.disneystreaming.neutrino.lang.JSerializable
import com.disneystreaming.neutrino.utils.JFunc
import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkEnv, TaskContext}

import java.nio.ByteBuffer
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

private[neutrino] object SerializableModuleGraphProvider extends StrictLogging {
    private final val encoder = Base64.getEncoder.withoutPadding()
    private final val decoder = Base64.getDecoder
    def getBroadcastName(graphName: String): String = s"property_${graphName}"

    private val graphMap = new ConcurrentHashMap[String, ModuleGraph]()
    private val broadcastMap = new ConcurrentHashMap[String, Broadcast[ModuleGraph]]()

    def createProvider(sparkSession: SparkSession, graph: ModuleGraph, graphName: String): ModuleGraphProvider = {
        val sparkContext = sparkSession.sparkContext
        val broadcastName = getBroadcastName(graphName)
        val b = sparkContext.broadcast(graph)

        // we need to keep the broadcast variable reference to keep it from being  GCed,
        // otherwise the broadcast id would be cleaned
        broadcastMap.put(broadcastName, b)

        sparkContext.setLocalProperty(
            broadcastName,
            encoder.encodeToString(
                SparkEnv.get.closureSerializer.newInstance().serialize(b).array()))
        graphMap.put(broadcastName, graph)

        new SerializableModuleGraphProvider(sparkSession, graph, broadcastName)
    }
}

private[neutrino] class SerializableModuleGraphProvider(
    private val sparkSession: SparkSession,
    @transient private val _graph: ModuleGraph,
    private val broadcastName: String)
    extends ModuleGraphProvider
        with JSerializable
        with StrictLogging {

    @transient
    private lazy val graph: ModuleGraph  = {
        if (_graph != null) {
            _graph
        } else {
            if (sparkSession.sparkContext != null) {
                // driver
                SerializableModuleGraphProvider.graphMap.get(broadcastName)
            } else {
                SerializableModuleGraphProvider.graphMap.computeIfAbsent(broadcastName, JFunc { k: String =>
                    val modelData = TaskContext.get().getLocalProperty(k)
                    if (modelData == null) {
                        throw new RuntimeException(s"can not read localproperty ${k}")
                    }

                    val b = SparkEnv.get.closureSerializer.newInstance().deserialize[Broadcast[ModuleGraph]](
                        ByteBuffer.wrap(
                            SerializableModuleGraphProvider.decoder.decode(
                                modelData)))
                    logger.info(s"executor graph broadcast id: ${b.id}")
                    b.value
                })
            }
        }
    }

    override def moduleGraph: ModuleGraph = graph
}
