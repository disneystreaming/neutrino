package com.hulu.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.Time
import org.apache.spark.streaming.dstream.DStream

import scala.reflect.ClassTag

package object streaming {
    implicit class StreamExtensions[T](private val stream: DStream[T]) extends AnyVal {
        def foreach(func: T => Unit): Unit = {
            stream.foreachRDD{rdd => rdd.foreach(e => func(e)) }
        }

        def foreachOnMaster(func: T => Unit): Unit = {
            stream.foreachRDD{rdd => rdd.collect().foreach(e => func(e)) }
        }

        def transformToValue[TV: ClassTag](func: RDD[T] => TV): DStream[TV] = {
            stream.transform{ rdd =>
                val value = func(rdd)
                rdd.sparkContext.makeRDD(Seq(value))
            }.reduce{(_, _) =>
                throw new RuntimeException("reduce should never happen since there is only one element in each RDD.")
            }
        }

        def transformToValue[TV: ClassTag](func: (RDD[T], Time) => TV): DStream[TV] = {
            stream.transform{ (rdd, time) =>
                val value = func(rdd, time)
                rdd.sparkContext.makeRDD(Seq(value))
            }.reduce{(_, _) =>
                throw new RuntimeException("reduce should never happen since there is only one element in each RDD.")
            }
        }
    }

    implicit class TraversableOnceStreamExtension[T, TC[T] <: TraversableOnce[T]](private val stream: DStream[TC[T]])
        extends AnyVal {
        def flatten(implicit ct: ClassTag[T]): DStream[T] = {
            stream.flatMap(t => t)
        }
    }

    implicit class OptionStreamExtension[T](private val stream: DStream[Option[T]])
        extends AnyVal {
        def flatten(implicit ct: ClassTag[T]): DStream[T] = {
            stream.flatMap(t => t)
        }
    }
}
