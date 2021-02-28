package com.hulu.spark.guice.modulegraph

import com.hulu.spark.JSerializable

trait ModuleGraphProvider extends JSerializable {
    def moduleGraph: ModuleGraph
}
