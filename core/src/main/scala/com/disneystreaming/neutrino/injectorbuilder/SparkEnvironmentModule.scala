package com.disneystreaming.neutrino.injectorbuilder

import com.disneystreaming.neutrino.{SingletonScope, SparkModule}
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.{SparkConf, SparkContext, SparkEnv}

import javax.inject.Provider

private[neutrino] class SparkEnvironmentModule (
    private val sparkSession: SparkSession) extends SparkModule {

    override def configure(): Unit = {
        this.bind[SparkSession].toInstance(sparkSession)
        this.bind[SparkContext].toProvider(new Provider[SparkContext] {
            override def get(): SparkContext = {
                if (sparkSession.sparkContext == null) {
                    throw new UnsupportedOperationException("SparkContext is only available in the driver")
                }

                sparkSession.sparkContext
            }
        })
        this.bind[SparkConf].toProvider(new Provider[SparkConf] {
            override def get(): SparkConf = SparkEnv.get.conf
        }).in[SingletonScope]
        this.bind[SQLContext].toProvider(new Provider[SQLContext] {
            override def get(): SQLContext = {
                if (sparkSession.sparkContext == null) {
                    throw new UnsupportedOperationException("SQLContext is only available in the driver")
                }

                sparkSession.sqlContext
            }
        })
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
