package com.hulu.neutrino.example

import redis.clients.jedis.Jedis
import redis.clients.jedis.commands.JedisCommands

import javax.inject.{Inject, Provider}

case class RedisConfig(host: String, port: Int)

class RedisConnectionProvider @Inject()(redisConfig: RedisConfig) extends Provider[JedisCommands] {
    override def get(): JedisCommands = {
        new Jedis(redisConfig.host, redisConfig.port)
    }
}
