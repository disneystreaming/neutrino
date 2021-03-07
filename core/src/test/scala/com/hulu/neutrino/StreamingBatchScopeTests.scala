package com.hulu.neutrino

import com.holdenkarau.spark.testing.StreamingSuiteBase
import com.hulu.neutrino.scope.StreamingBatch
import com.hulu.neutrino.sparktest.{LazySparkContext, NoCheckpoint, SparkSuiteTest}
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.DStream
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.junit.JUnitRunner

import javax.inject.Inject

trait Processor {
    def process(i: Int): Int
}

object BatchProcessor {
    @volatile
    var number = 0
}

class BatchProcessor @Inject()() extends Processor {
    private val offset = BatchProcessor.number
    BatchProcessor.number += 1

    override def process(i: Int): Int = offset + i
}

class TestModule extends SparkModule {
    override def configure(): Unit = {
        bind[Processor].withSerializableWrapper.to[BatchProcessor].in[StreamingBatch]
    }
}

@RunWith(classOf[JUnitRunner])
class StreamingBatchScopeTests extends SparkSuiteTest with StreamingSuiteBase with LazySparkContext with NoCheckpoint with BeforeAndAfterEach {
    override protected def afterEach(): Unit = {
        BatchProcessor.number = 0
    }

    override def numInputPartitions: Int = 1

    test("StreamingBatchScopeTests") {
        val sparkSession = SparkSession.builder().getOrCreate()
        val injector = sparkSession.newSingleInjector(new TestModule)
        val input = List(List(1, 2), List(3, 4), List(5, 6))
        val expected = List(List(1, 2), List(4, 5), List(7, 8))
        testOperation[Int, Int](input, (in: DStream[Int]) => in.map(i => injector.instance[Processor].process(i)), expected, ordered = false)
    }
}
