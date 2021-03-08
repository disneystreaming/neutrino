package com.hulu.neutrino.scope

import com.hulu.neutrino.SparkModule
import com.hulu.neutrino.annotation.scope.StreamingBatch

class StreamingBatchScopeModule extends SparkModule {
    override def configure(): Unit = {
        this.bindScope[StreamingBatch](new StreamingBatchScope)
    }
}
