package com.hulu.spark.streaming

import org.apache.spark.streaming.dstream.DStream

object DStreams {
    def union[T](streams: Seq[DStream[T]]): DStream[T] = {
        var stream = streams.head
        for (s <- streams.slice(1, streams.size)) {
            stream = stream.union(s)
        }

        stream
    }
}
