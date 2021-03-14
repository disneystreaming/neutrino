package com.hulu.neutrino.injectorbuilder

import com.google.common.base.Preconditions
import org.apache.spark.SparkContext
import org.apache.spark.streaming.StreamingContext

import scala.collection.mutable

private[neutrino] object SparkEnvironmentHolder {
    private val map = new mutable.WeakHashMap[SparkContext, StreamingContext]

    private[neutrino] def getStreamingContext(sparkContext: SparkContext): Option[StreamingContext] = {
        Preconditions.checkNotNull(sparkContext)

        map.get(sparkContext)
    }

    private[neutrino] def setStreamingContext(sparkContext: SparkContext, streamingContext: StreamingContext): Unit = {
        Preconditions.checkNotNull(sparkContext)
        Preconditions.checkNotNull(streamingContext)

        map.put(sparkContext, streamingContext)
    }
}
