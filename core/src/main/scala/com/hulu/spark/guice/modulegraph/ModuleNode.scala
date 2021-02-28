package com.hulu.spark.guice.modulegraph

import com.hulu.spark.guice.SerializableModule

trait ModulesCreator extends Serializable {
    def create(graphProperties: GraphProperties): Seq[SerializableModule]
}

private[guice] class ModuleNode(
   val parentIndex: Int,
   val modules: Option[Seq[SerializableModule]],
   val modulesCreator: Option[ModulesCreator]) extends Serializable
