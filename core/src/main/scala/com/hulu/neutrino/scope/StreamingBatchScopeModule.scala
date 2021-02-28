package com.hulu.neutrino.scope

import com.hulu.neutrino.SparkModule

class StreamingBatchScopeModule extends SparkModule {
    override def configure(): Unit = {
        this.bindScope[StreamingBatch](new StreamingBatchScope)
    }
}
