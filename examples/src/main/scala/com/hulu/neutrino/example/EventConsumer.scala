package com.hulu.neutrino.example

trait EventConsumer[T] {
    def consume(t: T)
}
