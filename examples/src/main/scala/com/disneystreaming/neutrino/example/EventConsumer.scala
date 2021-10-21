package com.disneystreaming.neutrino.example

trait EventConsumer[T] {
    def consume(t: T)
}
