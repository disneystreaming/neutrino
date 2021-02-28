package com.hulu.neutrino.modulegraph

import com.hulu.neutrino.SerializableModule

trait ModulesCreator extends Serializable {
    def create(graphProperties: GraphProperties): Seq[SerializableModule]
}

private[neutrino] class ModuleNode(
   val parentIndex: Int,
   val modules: Option[Seq[SerializableModule]],
   val modulesCreator: Option[ModulesCreator]) extends Serializable
