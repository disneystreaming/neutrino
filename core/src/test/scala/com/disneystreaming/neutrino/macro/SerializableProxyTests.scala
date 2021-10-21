package com.disneystreaming.neutrino.`macro`

import org.apache.commons.lang3.SerializationUtils
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.FunSuite
import org.scalatestplus.junit.JUnitRunner

trait SuperInterface[SS, SR] {
    def superExec(s: SS): SR
}

trait TestInterface[T, S, R, SS, SR] extends SuperInterface[SS, SR] {
    def exec(input1: T)(input2: S): R

    def exec2[P](p:P):Unit
}

trait TestInterface2[T, S, R, SR] extends SuperInterface[String, SR] {
    def exec(input1: T)(input2: S): R

    def exec2[P](p:P):Unit
}

class TestImpl2(mid: String) extends TestInterface2[String, String, String, String] {
    override def exec(input1: String)(input2: String): String = s"prefix_${mid}_${input1}_${input2}"

    override def exec2[P](p: P): Unit = Unit

    override def superExec(str: String): String = str + "Hello"
}

class TestImpl(mid: String) extends TestInterface[String, String, String, String, String] {
    override def exec(input1: String)(input2: String): String = s"prefix_${mid}_${input1}_${input2}"

    override def exec2[P](p: P): Unit = Unit

    override def superExec(str: String): String = str + "Hello"
}

// TestHelper needs to be a class to test the proxy doesn't include the outer class
class TestHelper {
    def getResult: Boolean = {
        val outer  = "outer"
        val proxy = SerializableProxy.createProxy[TestInterface[String, String, String, String, String]](() => new TestImpl(outer))
        val deserializedProxy = SerializationUtils.deserialize[TestInterface[String, String, String, String, String]](SerializationUtils.serialize(proxy))
        deserializedProxy.exec("Hello")("World") == "prefix_outer_Hello_World"
    }

    def getResult2: Boolean = {
        val outer  = "outer"
        val proxy = SerializableProxy.createProxy[TestInterface2[String, String, String, String]](() => new TestImpl2(outer))
        val deserializedProxy = SerializationUtils.deserialize[TestInterface2[String, String, String, String]](SerializationUtils.serialize(proxy))
        deserializedProxy.exec("Hello")("World") == "prefix_outer_Hello_World"
    }
}

// scalastyle:off
@RunWith(classOf[JUnitRunner])
class SerializableProxyTests extends FunSuite with MockFactory {
    test("SerializableProxyTests 1") {
        val result = new TestHelper().getResult
        assert(result)
    }

    test("SerializableProxyTests 2") {
        val result = new TestHelper().getResult2
        assert(result)
    }
}
// scalastyle:on
