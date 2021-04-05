package com.hulu.neutrino.example

import com.hulu.neutrino.annotation.scope.StreamingBatch
import com.hulu.neutrino.{SingletonScope, SparkModule}
import redis.clients.jedis.commands.JedisCommands

class FilterModule(redisConfig: RedisConfig) extends SparkModule {
    override def configure(): Unit = {
        bind[RedisConfig].toInstance(redisConfig)
        bind[JedisCommands].toProvider[RedisConnectionProvider].in[SingletonScope]
        bind[EventFilter[TestEvent]].withSerializableProxy.to[RedisUserWhiteListsEventFilter].in[StreamingBatch]
    }
}
