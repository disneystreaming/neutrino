package com.hulu.spark.utils

import org.apache.spark.SparkEnv
import org.apache.spark.streaming.StreamingContext

trait Clock {
    def currentTime: Long
}

class SparkClock extends Clock {
    override def currentTime: Long = {
        method.invoke(clock).asInstanceOf[Long]
    }

    private lazy val method = {
        clock.getClass.getMethod("getTimeMillis")
    }

    private def getFieldValue[T](obj: Any, fieldName: String): T = {
        val methods = obj.getClass.getMethods.toSeq
        methods.filter(_.getName.equals(fieldName)).head.invoke(obj).asInstanceOf[T]
    }

    private lazy val clock = {
        StreamingContext.getActive().map{ ssc =>
            getFieldValue[Any](getFieldValue[Any](ssc, "scheduler"), "clock")
        }.getOrElse {
            // the code snippet below is copied from
            // https://github.com/apache/spark/blob/master/streaming/src/main/scala/org/apache/spark/streaming/scheduler/JobGenerator.scala
            val clockClass = SparkEnv.get.conf.get(
                "spark.streaming.clock", "org.apache.spark.util.SystemClock")
            try {
                Class.forName(clockClass).newInstance()
            } catch {
                case e: ClassNotFoundException if clockClass.startsWith("org.apache.spark.streaming") =>
                    val newClockClass = clockClass.replace("org.apache.spark.streaming", "org.apache.spark")
                    Class.forName(newClockClass).newInstance()
            }
        }
    }
}

object SparkClock extends Clock {
    @volatile
    private var clock: Clock = new SparkClock()

    override def currentTime: Long = {
        clock.currentTime
    }

    def reset(c : Clock): Clock = {
        val oldClock = clock
        clock = c
        oldClock
    }

    def reset(): Clock = {
        reset(new SparkClock())
    }
}
