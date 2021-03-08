package com.hulu.neutrino.example

import com.hulu.neutrino._
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.Duration
import org.apache.spark.streaming.dstream.DStream


object StreamingJobWithCheckpoint {
    def main(args: Array[String]): Unit = {
        val sparkSession = SparkSession.builder()
            .appName("StreamingJobWithCheckpoint")
            .getOrCreate()
        val injectorBuilder = sparkSession.newInjectorBuilder()
        val rootInjector = injectorBuilder.newRootInjector(Modules.bindModules:_*)
        injectorBuilder.prepareInjectors() // Don't forget to call this before getting any instance from injector

        // Don't call StreamingContext.getOrCreate directly
        val streamingContext = sparkSession.getOrCreateStreamingContext("hdfs://HOST/checkpointpath", session => {
            // Don't call the constructor directly
            val streamContext = session.newStreamingContext(Duration(1000*30))
            rootInjector.instance[DStream[TestEvent]]
                .filter(e => rootInjector.instance[EventFilter[TestEvent]].filter(e))
                .foreachRDD(_.foreach(e => rootInjector.instance[EventConsumer[TestEvent]].consume(e)))
            streamContext
        })

        streamingContext.start()
        streamingContext.awaitTermination()
    }
}
