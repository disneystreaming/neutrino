package com.disneystreaming.neutrino.example

import org.apache.kafka.clients.producer.{Producer, ProducerRecord}

import javax.inject.Inject

case class KafkaTopic(topic: String)

class KafkaEventConsumer[T] @Inject()(producer: Producer[String, String], kafkaTopic: KafkaTopic) extends EventConsumer[T] {
    override def consume(t: T): Unit = {
        val record = new ProducerRecord[String, String](kafkaTopic.topic, Json.mapper.writeValueAsString(t))
        producer.send(record)
    }
}
