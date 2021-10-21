package com.disneystreaming.neutrino.graph

import com.disneystreaming.neutrino.SerializableModule
import com.disneystreaming.neutrino.lang.JSerializable

trait ModulesCreator extends JSerializable {
    def create(graphProperties: GraphProperties): Seq[SerializableModule]
}

private[neutrino] case class ModuleNode(
   parentIndex: Int,
   modules: Option[Seq[SerializableModule]],
   modulesCreator: Option[ModulesCreator])
