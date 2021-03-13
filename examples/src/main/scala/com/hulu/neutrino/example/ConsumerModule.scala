package com.hulu.neutrino.example

import com.hulu.neutrino.{SingletonScope, SparkModule}
import org.apache.kafka.clients.producer.Producer

class ConsumerModule(kafkaProducerConfig: KafkaProducerConfig, topic: String) extends SparkModule {
    override def configure(): Unit = {
        bind[KafkaTopic].toInstance(KafkaTopic(topic))
        bind[KafkaProducerConfig].toInstance(kafkaProducerConfig)
        bind[Producer[String, String]].toProvider[KafkaProducerProvider].in[SingletonScope]
        bind[EventConsumer[TestEvent]].withSerializableProxy.to[KafkaEventConsumer[TestEvent]].in[SingletonScope]
    }
}
