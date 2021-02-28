package com.hulu.spark.guice.modulegraph

import com.hulu.spark.guice.SerializableModule

import scala.collection.mutable.ListBuffer

object ModuleGraphBuilder {

    trait Builder {
        def rootModules(modules: Seq[SerializableModule]): Int = {
            rootModules(modules, null)
        }

        def rootModules(modules: Seq[SerializableModule], modulesCreator: ModulesCreator): Int

        def createChildInjector(parentId: Int, modules: Seq[SerializableModule]): Int = {
            createChildInjector(parentId, modules, null)
        }

        def createChildInjector(parentId: Int, modules: Seq[SerializableModule], modulesCreator: ModulesCreator): Int

        def build(): ModuleGraph
    }

    private[guice] class GraphBuilder extends Builder {
        private val nodes: ListBuffer[ModuleNode] = ListBuffer[ModuleNode]()

        override def rootModules(modules: Seq[SerializableModule], modulesCreator: ModulesCreator): Int = {
            createChildInjector(-1, modules, modulesCreator)
        }

        override def createChildInjector(parentId: Int, modules: Seq[SerializableModule], modulesCreator: ModulesCreator): Int = {
            if (parentId < -1 || parentId >= nodes.size) {
                throw new RuntimeException(s"The parent injector id $parentId is not found")
            }

            if (modules == null && modulesCreator == null) {
                throw new RuntimeException("both modules and modulesCreator are null")
            }

            var node = new ModuleNode(parentId, Option(modules), Option(modulesCreator))
            nodes += node
            nodes.size - 1
        }

        override def build(): ModuleGraph = {
            new SerializableModuleGraph(nodes)
        }
    }

    def newBuilder(): Builder = {
        new GraphBuilder
    }
}
