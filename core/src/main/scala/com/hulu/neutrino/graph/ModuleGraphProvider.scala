package com.hulu.neutrino.graph

import com.hulu.neutrino.lang.JSerializable

private[neutrino] trait ModuleGraphProvider extends JSerializable {
    def moduleGraph: ModuleGraph
}
