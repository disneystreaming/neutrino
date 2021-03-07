package com.hulu.neutrino.sparktest

import com.holdenkarau.spark.testing.StreamingSuiteBase
import org.scalatest.Suite

trait LazySparkContext extends StreamingSuiteBase { suite: Suite =>
    override lazy val conf = {
        super.conf
            .set("spark.ui.enabled", "false")
            .set("spark.app.id", appID)
            .set("spark.driver.host", "localhost")
    }
}

