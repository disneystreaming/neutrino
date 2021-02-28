package com.hulu.neutrino

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.StreamingContext

object SparkEnvironmentHolder {
    private var session: SparkSession = _
    private var streaming: StreamingContext = _

    private var isInDriver = false

    private[neutrino] def setSparkSession(sparkSession: SparkSession): Unit = {
        session = sparkSession
    }

    def sparkSession: SparkSession = {
        session
    }

    def sparkContext: SparkContext = {
        session.sparkContext
    }

    private[neutrino] def setStreamingContext(streamingContext: StreamingContext): Unit = {
        streaming = streamingContext
    }

    private[neutrino] def setDriver(): Unit = {
        isInDriver = true
    }

    def isDriver: Boolean = isInDriver

    def streamingContext: StreamingContext = {
        streaming
    }
}
