package com.hulu.neutrino

import com.google.inject.name.Names
import com.hulu.neutrino.annotation.Wrapper
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.FunSuite
import org.scalatestplus.junit.JUnitRunner

// scalastyle:off
trait TestInterface {
    def exec(input: String): String
}

class TestImpl extends TestInterface {
    override def exec(input: String): String = "Hello"
}

class SparkModuleTest extends SparkModule {
    override def configure(): Unit = {
        install(new SparkPrivateModule { inner =>
            override def configure(): Unit = {
                // The keyword "this" need to be here to call the methods of the inner class instead of the outer ones
                inner.bind[TestInterface].annotatedWith[Wrapper].withSerializableProxy.to[TestImpl].in[SingletonScope]
                inner.bind[TestInterface].annotatedWith(classOf[Wrapper]).withSerializableProxy.to[TestImpl].in[SingletonScope]
                inner.bind[TestInterface].annotatedWithName("hello").withSerializableProxy.to[TestImpl].in[SingletonScope]
                inner.bind[TestInterface].annotatedWith(Names.named("world")).withSerializableProxy.to[TestImpl].in[SingletonScope]
            }
        })
    }
}

@RunWith(classOf[JUnitRunner])
class SerializableWrapperModuleTests extends FunSuite with MockFactory {
    test("SerializableWrapperModuleTests") {
        new SparkModuleTest
    }
}
// scalastyle:on
