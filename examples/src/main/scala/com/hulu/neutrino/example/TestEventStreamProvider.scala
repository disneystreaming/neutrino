package com.hulu.neutrino.example

import com.hulu.neutrino.{SingletonScope, SparkModule}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}

import javax.inject.{Inject, Provider}

case class KafkaConsumerConfig(properties: Map[String, String], topics: Seq[String])

class TestEventStreamProvider @Inject()(streamingContext: StreamingContext, kafkaConfig: KafkaConsumerConfig)
    extends Provider[DStream[TestEvent]] {
    override def get(): DStream[TestEvent] = {
        KafkaUtils
            .createDirectStream[String, String](
                streamingContext,
                LocationStrategies.PreferConsistent,
                ConsumerStrategies.Subscribe[String, String](kafkaConfig.topics.toSet, kafkaConfig.properties))
            .map { record =>
                Json.mapper.readValue[TestEvent](record.value())
            }
    }
}

class TestEventStreamModule(kafkaConfig: KafkaConsumerConfig) extends SparkModule {
    override def configure(): Unit = {
        bind[KafkaConsumerConfig].toInstance(kafkaConfig)
        bind[DStream[TestEvent]].toProvider[TestEventStreamProvider].in[SingletonScope]
    }
}
