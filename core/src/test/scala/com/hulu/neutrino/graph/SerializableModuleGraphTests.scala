package com.hulu.neutrino.graph

import com.google.inject.Key
import com.google.inject.name.Names
import com.hulu.neutrino.{SerializableModule, SparkModule}
import com.hulu.neutrino.lang.JSerializable
import org.apache.commons.lang3.SerializationUtils
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.FunSuite
import org.scalatestplus.junit.JUnitRunner

trait StringInterface {
    def getString: String
}

class StringImpl(str: String) extends StringInterface {
    override def getString: String = str
}

class StringModule(bindingName: String, str: String) extends SparkModule {
    override def configure(): Unit = {
        if (bindingName != null) {
            bind[StringInterface].annotatedWithName(bindingName).toInstance(new StringImpl(str))
        } else {
            bind[StringInterface].toInstance(new StringImpl(str))
        }
    }
}

class ModulesCreatorImpl(name: String, value: String) extends ModulesCreator {
    override def create(graphProperties: GraphProperties): Seq[SerializableModule] = {
        Seq(new SparkModule {
            override def configure(): Unit = {
                graphProperties.setProperty(name, value)
            }
        })
    }
}

@RunWith(classOf[JUnitRunner])
private[neutrino] class SerializableModuleGraphTests extends FunSuite with MockFactory {
    test("SerializableModuleGraphTests") {
        assertThrows[UnsupportedOperationException] {
            ModuleGraphBuilder.newBuilder().build()
        }

        val builder = ModuleGraphBuilder.newBuilder()
        val parentIndex = builder.rootModules(
            Seq(new StringModule("root", "root_str")),
            new ModulesCreatorImpl("pname", "pvalue"))
        val child1Index = builder.createChildInjector(parentIndex, Seq(new StringModule("Child1", "Child1_str")))
        val child2Index = builder.createChildInjector(parentIndex, Seq(new StringModule("Child2", "Child2_str")))

        val originalGraph = builder.build()

        assert(originalGraph.property[String]("pname").get == "pvalue")

        val serializableGraph = SerializationUtils.deserialize(
            SerializationUtils.serialize(originalGraph.asInstanceOf[JSerializable])).asInstanceOf[ModuleGraph]

        verifyGraph(serializableGraph, parentIndex, child1Index, child2Index)
    }

    def verifyGraph(graph: ModuleGraph, parentIndex: Int, child1Index: Int, child2Index: Int): Unit = {
        assert(graph.property[String]("pname").get == "pvalue")

        assert(graph.injector(parentIndex)
            .getInstance(Key.get(classOf[StringInterface], Names.named("root")))
            .getString == "root_str")
        assert(graph.injector(child1Index)
            .getInstance(Key.get(classOf[StringInterface], Names.named("Child1")))
            .getString == "Child1_str")
        assert(graph.injector(child2Index)
            .getInstance(Key.get(classOf[StringInterface], Names.named("Child2")))
            .getString == "Child2_str")
    }
}
