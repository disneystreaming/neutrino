package com.disneystreaming.neutrino.sparktest

import com.holdenkarau.spark.testing.StreamingSuiteBase

trait NoCheckpoint {
    this: StreamingSuiteBase =>

    // scalastyle:off
    // disable checkpoint
    override lazy val checkpointDir: String = null
    // scalastyle:on
}
