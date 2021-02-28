package com.hulu.spark.guice.scope

import com.hulu.spark.guice.SparkModule

class StreamingBatchScopeModule extends SparkModule {
    override def configure(): Unit = {
        this.bindScope[StreamingBatch](new StreamingBatchScope)
    }
}
