package com.hulu.neutrino.scope

import com.google.inject.{Key, Provider, Scope}
import org.apache.spark.TaskContext

import java.util.concurrent.atomic.AtomicReference
import scala.collection.mutable

object StreamingBatchScope {
    // the task local property for batch time
    // https://github.com/apache/spark/blob/master/streaming/src/main/scala/org/apache/spark/streaming/scheduler/JobScheduler.scala
    final val BATCH_TIME_PROPERTY_KEY = "spark.streaming.internal.batchTime"
}

case class BatchValue(batchTime: Long, value: Any)

class StreamingBatchScope extends Scope {
    private val locks = mutable.Map[Key[_], Object]()
    private val batchValues = mutable.Map[Key[_], AtomicReference[BatchValue]]()

    override def scope[T](key: Key[T], unscoped: Provider[T]): Provider[T] = {
        if (!batchValues.contains(key)) {
            batchValues.put(key, new AtomicReference[BatchValue]())
            locks.put(key, new Object)
        }

        new Provider[T] {
            override def get(): T = {
                val taskContext = TaskContext.get()
                if (taskContext == null) {
                    throw new UnsupportedOperationException(s"the batched scope instance should only be retrieved in batch context")
                }

                val batchTime = taskContext.getLocalProperty(StreamingBatchScope.BATCH_TIME_PROPERTY_KEY).toLong
                val valueRef = batchValues(key)
                val oldBatch = valueRef.get()
                if (oldBatch != null && oldBatch.batchTime == batchTime) {
                    oldBatch.value.asInstanceOf[T]
                } else {
                    val lock = locks.get(key)
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
