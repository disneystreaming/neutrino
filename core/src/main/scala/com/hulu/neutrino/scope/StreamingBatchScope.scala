package com.hulu.neutrino.scope

import com.google.inject.{Key, Provider, Scope}
import org.apache.spark.{SparkContext, TaskContext}

import java.util.concurrent.atomic.AtomicReference
import scala.collection.mutable

object StreamingBatchScope {
    // the task local property for batch time
    // https://github.com/apache/spark/blob/master/streaming/src/main/scala/org/apache/spark/streaming/scheduler/JobScheduler.scala
    final val BATCH_TIME_PROPERTY_KEY = "spark.streaming.internal.batchTime"
}

case class BatchValue(batchTime: Long, value: Any)

class StreamingBatchScope extends Scope {
    private var id = 0

    private val locks = mutable.Map[Int, Object]()
    private val batchValues = mutable.Map[Int, AtomicReference[BatchValue]]()

    override def scope[T](key: Key[T], unscoped: Provider[T]): Provider[T] = {
        val currentId = id
        id += 1

        batchValues.put(currentId, new AtomicReference[BatchValue]())
        locks.put(currentId, new Object)

        new Provider[T] {
            override def get(): T = {
                val batchTimeStr: String =
                    if (TaskContext.get() != null) {
                        // executor
                        TaskContext.get().getLocalProperty(StreamingBatchScope.BATCH_TIME_PROPERTY_KEY)
                    } else {
                        // driver
                        SparkContext.getOrCreate().getLocalProperty(StreamingBatchScope.BATCH_TIME_PROPERTY_KEY)
                    }

                if (batchTimeStr == null) {
                    throw new UnsupportedOperationException(s"the batched scope instance should only be retrieved in streaming batch context")
                }

                val batchTime = batchTimeStr.toLong
                val valueRef = batchValues(currentId)
                val oldBatch = valueRef.get()
                if (oldBatch != null && oldBatch.batchTime == batchTime) {
                    oldBatch.value.asInstanceOf[T]
                } else {
                    val lock = locks.get(currentId)
                    var oldValue: Any = null
                    var result: Any = null
                    lock.synchronized {
                        if (valueRef.get() != null && valueRef.get().batchTime == batchTime) {
                            result = valueRef.get().value.asInstanceOf[T]
                        } else {
                            result = unscoped.get()
                            val newValue = BatchValue(batchTime, result)
                            if (valueRef.get() != null) {
                                oldValue = valueRef.get().value
                            }
                            valueRef.set(newValue)
                        }
                    }

                    if (oldValue != null && oldValue.isInstanceOf[AutoCloseable]) {
                        oldValue.asInstanceOf[AutoCloseable].close()
                    }

                    result.asInstanceOf[T]
                }
            }
        }
    }
}
