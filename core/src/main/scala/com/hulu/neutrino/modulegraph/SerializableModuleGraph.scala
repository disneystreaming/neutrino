package com.hulu.neutrino.modulegraph

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import com.google.common.base.Preconditions
import com.google.inject.{Guice, Injector}
import com.hulu.neutrino.lang.JSerializable
import com.hulu.neutrino.SerializableModule

import java.io.{IOException, ObjectInputStream, ObjectOutputStream}
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait GraphProperties {
    def setProperty[T](name: String, value: T): Unit
    def getProperty[T](name: String): Option[T]
}

class SerializableModuleGraph(private var nodes: Seq[ModuleNode])
    extends ModuleGraph
    with JSerializable
    with KryoSerializable
    with GraphProperties {
    Preconditions.checkNotNull(nodes)

    private[neutrino] def this() {
        this(Seq())
    }

    @transient
    private var injectors: ListBuffer[Injector] = _

    @transient
    private var properties: mutable.Map[String, Any] = _

    @transient
    @volatile
    private var built = false

    buildInjectors()

    private def buildInjectors(): Unit = {
        if (!built) {
            properties = mutable.Map[String, Any]()
            injectors = new ListBuffer[Injector]()

            nodes.indices.foreach { i =>
                val node = nodes(i)
                val parentIndex = node.parentIndex

                val injector = if (parentIndex == -1) {
                    Guice.createInjector(getModules(node).asJava)
                } else {
                    injectors(parentIndex).createChildInjector(getModules(node).asJava)
                }

                injectors += injector
            }

            built = true
        }
    }

    private def getModules(node: ModuleNode): Seq[SerializableModule] = {
        node.modules.getOrElse(Seq()) ++ node.modulesCreator.map(c => c.create(this)).getOrElse(Seq())
    }

    def getInjector(injectorIndex: Int): Injector = {
        injectors(injectorIndex)
    }

    @throws[IOException]
    private def writeObject(out: ObjectOutputStream): Unit = {
        out.defaultWriteObject()
    }

    @throws[IOException]
    @throws[ClassNotFoundException]
    private def readObject(in: ObjectInputStream): Unit = {
        in.defaultReadObject()
        buildInjectors()
    }

    override def write(kryo: Kryo, output: Output): Unit = {
        kryo.writeObject(output, nodes)
    }


    override def read(kryo: Kryo, input: Input): Unit = {
        nodes = kryo.readClassAndObject(input).asInstanceOf[Seq[ModuleNode]]
        buildInjectors()
    }

    override def injector(id: Int): Injector = {
        require(id >=0 && id < injectors.size)

        injectors(id)
    }

    override def property[T](name: String): Option[T] = {
        getProperty(name)
    }

    override def setProperty[T](name: String, value: T): Unit = {
        properties.put(name, value)
    }

    override def getProperty[T](name: String): Option[T] = {
        properties.get(name).map(p => p.asInstanceOf[T])
    }
}
