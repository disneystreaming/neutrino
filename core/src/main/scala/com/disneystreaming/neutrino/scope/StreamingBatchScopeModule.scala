package com.disneystreaming.neutrino.scope

import com.disneystreaming.neutrino.SparkModule
import com.disneystreaming.neutrino.annotation.scope.StreamingBatch

class StreamingBatchScopeModule extends SparkModule {
    override def configure(): Unit = {
        this.bindScope[StreamingBatch](new StreamingBatchScope)
    }
}
