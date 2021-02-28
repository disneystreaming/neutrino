package com.hulu.neutrino.modulegraph

import com.hulu.neutrino.lang.JSerializable

trait ModuleGraphProvider extends JSerializable {
    def moduleGraph: ModuleGraph
}
