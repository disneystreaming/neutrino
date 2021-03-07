package com.hulu.neutrino.example

import org.apache.kafka.clients.producer.{KafkaProducer, Producer}

import javax.inject.{Inject, Provider}
import scala.jdk.CollectionConverters._

case class KafkaProducerConfig(properties: Map[String, Object])

class KafkaProducerProvider @Inject()(kafkaProducerConfig: KafkaProducerConfig) extends Provider[Producer[String, String]] {
    override def get(): Producer[String, String] = {
        new KafkaProducer(kafkaProducerConfig.properties.asJava)
    }
}
