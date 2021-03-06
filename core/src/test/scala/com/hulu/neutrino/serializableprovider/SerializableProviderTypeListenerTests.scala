package com.hulu.neutrino.serializableprovider

import com.google.inject.matcher.Matchers
import com.google.inject.name.{Named, Names}
import com.google.inject.{AbstractModule, Guice, Key, Provider}
import com.hulu.neutrino.annotation.InjectSerializableProvider
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.FunSuite
import org.scalatestplus.junit.JUnitRunner

import javax.inject.Inject

class StringProvider @Inject()() extends Provider[String] {
    private var internalStrProvider: Provider[String] = _

    @InjectSerializableProvider
    def setStr(@Named("original") strProvider: Provider[String]): Unit = {
        this.internalStrProvider = strProvider
    }

    override def get(): String = {
        this.internalStrProvider.get() + "_provider"
    }
}

@RunWith(classOf[JUnitRunner])
class SerializableProviderTypeListenerTests extends FunSuite with MockFactory {
    test("SerializableProviderTypeListenerTests") {
        val providerFactory = new SerializableProviderFactory() {
            override def getSerializableProvider[T](rawProvider: Provider[T]): SerializableProvider[T] = {
                new SerializableProvider[T] {
                    override def get(): T = {
                        val obj = rawProvider.get()
                        if (obj.isInstanceOf[String]) {
                            (obj + "_wrapped").asInstanceOf[T]
                        } else {
                            obj
                        }
                    }
                }
            }
        }

        val module = new AbstractModule() {
            override def configure(): Unit = {
                this.bindListener(Matchers.any(), new SerializableProviderTypeListener(providerFactory))
                this.bind(classOf[String]).annotatedWith(Names.named("original")).toInstance("world")
                this.bind(classOf[StringProvider])
                this.bind(classOf[String]).annotatedWith(Names.named("another")).toProvider(classOf[StringProvider])
            }
        }

        val injector = Guice.createInjector(module)
        assert(injector.getInstance(Key.get(classOf[String], Names.named("original"))) == "world")
        assert(injector.getInstance(Key.get(classOf[String], Names.named("another"))) == "world_wrapped_provider")
    }
}
