package com.hulu.neutrino

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.StreamingContext

import javax.inject.Provider

class StreamingContextProvider extends Provider[StreamingContext] {
    override def get(): StreamingContext = {
        if (!SparkEnvironmentHolder.isDriver) {
            throw new RuntimeException("StreamingContext is only available in driver")
        }

        val streaming = SparkEnvironmentHolder.streamingContext
        if (streaming == null) {
            throw new RuntimeException("StreamingContext is not available, did you forget to call sparkSession.newStreamingContext ?")
        }

        streaming
    }
}

private[neutrino] class SparkEnvironmentModule (
    private val sparkSession: SparkSession) extends SparkModule {
    private val sparkConf = sparkSession.sparkContext.getConf

    override def configure(): Unit = {
        this.bind[SparkSession].toInstance(sparkSession)
        this.bind[SparkConf].toInstance(sparkConf)
        this.bind[StreamingContext].toProvider[StreamingContextProvider].in[SingletonScope]
    }
}
