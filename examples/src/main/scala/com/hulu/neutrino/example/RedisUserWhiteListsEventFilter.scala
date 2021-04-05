package com.hulu.neutrino.example

import redis.clients.jedis.commands.JedisCommands

import javax.inject.Inject
import scala.jdk.CollectionConverters._

class RedisUserWhiteListsEventFilter @Inject()(jedis: JedisCommands) extends EventFilter[TestEvent] {
    private lazy val userIdSet = {
        jedis.hkeys("users").asScala
    }

    override def filter(t: TestEvent): Boolean = {
        t.userId != null && userIdSet.contains(t.userId)
    }
}
