package com.disneystreaming.neutrino.graph

import com.disneystreaming.neutrino.lang.JSerializable

private[neutrino] trait ModuleGraphProvider extends JSerializable {
    def moduleGraph: ModuleGraph
}
