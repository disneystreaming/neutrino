package com.hulu.neutrino

import org.apache.spark.{SparkConf, SparkEnv}
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.streaming.StreamingContext

import javax.inject.Provider

private[neutrino] class SparkEnvironmentModule (
    private val sparkSession: SparkSession) extends SparkModule {

    override def configure(): Unit = {
        this.bind[SparkSession].toInstance(sparkSession)
        this.bind[SparkConf].toProvider(new Provider[SparkConf] {
            override def get(): SparkConf = SparkEnv.get.conf
        }).in[SingletonScope]
        this.bind[SQLContext].toInstance(sparkSession.sqlContext)
        this.bind[StreamingContext].toProvider(new Provider[StreamingContext] {
            override def get(): StreamingContext = {
                if (sparkSession.sparkContext == null) {
                    throw new UnsupportedOperationException("StreamingContext is only available in the driver")
                }

                SparkEnvironmentHolder.getStreamingContext(sparkSession.sparkContext).get
            }
        }).in[SingletonScope]
    }
}
