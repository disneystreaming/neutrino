package com.hulu.neutrino.graph

import com.hulu.neutrino.SerializableModule
import com.hulu.neutrino.lang.JSerializable

trait ModulesCreator extends JSerializable {
    def create(graphProperties: GraphProperties): Seq[SerializableModule]
}

private[neutrino] case class ModuleNode(
   parentIndex: Int,
   modules: Option[Seq[SerializableModule]],
   modulesCreator: Option[ModulesCreator])
