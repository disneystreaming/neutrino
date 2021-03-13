package com.hulu.neutrino.example

import com.hulu.neutrino.annotation.scope.StreamingBatch
import com.hulu.neutrino.{SingletonScope, SparkModule}

class FilterModule(dbConfig: DbConfig) extends SparkModule {
    override def configure(): Unit = {
        bind[DbConfig].toInstance(dbConfig)
        bind[java.sql.Connection].toProvider[DbConnectionProvider].in[SingletonScope]
        bind[EventFilter[TestEvent]].withSerializableProxy.to[DbUserWhiteListsEventFilter].in[StreamingBatch]
    }
}
