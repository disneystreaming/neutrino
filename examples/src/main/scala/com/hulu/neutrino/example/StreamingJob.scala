package com.hulu.neutrino.example

import com.hulu.neutrino._
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.Duration
import org.apache.spark.streaming.dstream.DStream

object Modules {
    def bindModules: Seq[SerializableModule] = {
        val kafkaConsumerConfig = KafkaConsumerConfig(properties = Map(
            "group.id" -> "consumergroup",
            "bootstrap.servers" -> "server:port"),
            topics = Seq("EventTopic"))
        val kafkaProducerConfig = KafkaProducerConfig(Map(
            "bootstrap.servers" -> "server:port"))
        val dbConfig = DbConfig("jdbc:myDriver:myDatabase", "user", "pass")
        Seq(new TestEventStreamModule(kafkaConsumerConfig),
        new FilterModule(dbConfig),
        new ConsumerModule(kafkaProducerConfig, "targetTopic"))
    }
}

object StreamingJob {
    def main(args: Array[String]): Unit = {
        val sparkSession = SparkSession.builder()
            .appName("StreamingJob")
            .getOrCreate()
        val injectorBuilder = sparkSession.newInjectorBuilder()
        val rootInjector = injectorBuilder.newRootInjector(Modules.bindModules:_*)
        injectorBuilder.prepareInjectors() // Don't forget to call this before getting any instance from injector

        val streamingContext = sparkSession.newStreamingContext(Duration(1000*30))
        rootInjector.instance[DStream[TestEvent]]
            .filter(e => rootInjector.instance[EventFilter[TestEvent]].filter(e))
            .foreachRDD(_.foreach(e => rootInjector.instance[EventConsumer[TestEvent]].consume(e)))

        streamingContext.start()
        streamingContext.awaitTermination()
    }
}
