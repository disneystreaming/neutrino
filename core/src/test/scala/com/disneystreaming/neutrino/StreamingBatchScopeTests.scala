package com.disneystreaming.neutrino

import com.holdenkarau.spark.testing.StreamingSuiteBase
import com.disneystreaming.neutrino.annotation.scope.StreamingBatch
import com.disneystreaming.neutrino.sparktest.{LazySparkContext, NoCheckpoint, SparkSuiteTest}
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.DStream
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.junit.JUnitRunner

import javax.inject.Inject

trait Processor {
    def process(i: Int): Int
}

trait SubProcessor extends Processor

object BatchProcessor {
    @volatile
    var number = 0
}

final class BatchProcessor @Inject()() extends SubProcessor {
    private val offset = BatchProcessor.number
    BatchProcessor.number += 1

    override def process(i: Int): Int = offset + i
}

class TestModuleWithSerializableProxy extends SparkModule {
    override def configure(): Unit = {
        bind[Processor].withSerializableProxy.to[BatchProcessor].in[StreamingBatch]
    }
}

class TestSubmoduleWithSerializableProxy extends SparkModule {
    override def configure(): Unit = {
        bind[SubProcessor].withSerializableProxy.to[BatchProcessor].in[StreamingBatch]
    }
}

class TestModuleWithSerializableProvider extends SparkModule {
    override def configure(): Unit = {
        bind[BatchProcessor].in[StreamingBatch]
        bindSerializableProvider[BatchProcessor]()
    }
}

@RunWith(classOf[JUnitRunner])
class StreamingBatchScopeTests extends SparkSuiteTest with StreamingSuiteBase with LazySparkContext with NoCheckpoint with BeforeAndAfterEach {
    override protected def afterEach(): Unit = {
        BatchProcessor.number = 0
    }

    override def numInputPartitions: Int = 1

    test("SerializableProxy injector serializable") {
        val sparkSession = SparkSession.builder().getOrCreate()
        val injector = sparkSession.newSingleInjector("test 1", new TestModuleWithSerializableProxy)
        val input = List(List(1, 2), List(3, 4), List(5, 6))
        val expected = List(List(1, 2), List(4, 5), List(7, 8))
        testOperation[Int, Int](
            input,
            (in: DStream[Int]) => in.map(i => injector.instance[Processor].process(i)),
            expected, ordered = false)
    }

    test("SerializableProxy injector serializable with child injector") {
        val sparkSession = SparkSession.builder().getOrCreate()
        val builder = sparkSession.newInjectorBuilder("test 1 for child injector")
        val injector = builder.newRootInjector(new TestModuleWithSerializableProxy)
        val childInjector = injector.createChildInjector(new TestSubmoduleWithSerializableProxy)
        builder.completeBuilding()
        val input = List(List(1, 2), List(3, 4), List(5, 6))
        val expected = List(List(1, 2), List(4, 5), List(7, 8))
        testOperation[Int, Int](
            input,
            (in: DStream[Int]) => in.map(i => childInjector.instance[SubProcessor].process(i)),
            expected, ordered = false)
    }

    test("SerializableProxy proxy serializable") {
        val sparkSession = SparkSession.builder().getOrCreate()
        val injector = sparkSession.newSingleInjector("test 2", new TestModuleWithSerializableProxy)
        val input = List(List(1, 2), List(3, 4), List(5, 6))
        val expected = List(List(1, 2), List(4, 5), List(7, 8))
        val proxy = injector.instance[Processor]
        testOperation[Int, Int](
            input,
            (in: DStream[Int]) => in.map(i => proxy.process(i)),
            expected,
            ordered = false)
    }

    test("SerializableProvider injector serializable") {
        val sparkSession = SparkSession.builder().getOrCreate()
        val injector = sparkSession.newSingleInjector("test 3", new TestModuleWithSerializableProvider)
        val input = List(List(1, 2), List(3, 4), List(5, 6))
        val expected = List(List(1, 2), List(4, 5), List(7, 8))
        testOperation[Int, Int](
            input,
            (in: DStream[Int]) => in.map(i => injector.instance[BatchProcessor].process(i)),
            expected, ordered = false)
    }

    test("SerializableProvider injector serializable and SerializableProvider injectable") {
        val sparkSession = SparkSession.builder().getOrCreate()
        val injector = sparkSession.newSingleInjector("test 4", new TestModuleWithSerializableProvider)
        val input = List(List(1, 2), List(3, 4), List(5, 6))
        val expected = List(List(1, 2), List(4, 5), List(7, 8))
        testOperation[Int, Int](
            input,
            (in: DStream[Int]) => in.map(i => injector.instance[SerializableProvider[BatchProcessor]].get().process(i)),
            expected,
            ordered = false)
    }

    test("SerializableProvider serializable") {
        val sparkSession = SparkSession.builder().getOrCreate()
        val injector = sparkSession.newSingleInjector("test 5", new TestModuleWithSerializableProvider)
        val input = List(List(1, 2), List(3, 4), List(5, 6))
        val expected = List(List(1, 2), List(4, 5), List(7, 8))
        val provider = injector.instance[SerializableProvider[BatchProcessor]]
        testOperation[Int, Int](
            input,
            (in: DStream[Int]) => in.map(i => provider.get().process(i)),
            expected,
            ordered = false)
    }
}
