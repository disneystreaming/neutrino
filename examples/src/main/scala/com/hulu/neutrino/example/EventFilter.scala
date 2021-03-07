package com.hulu.neutrino.example

trait EventFilter[T] {
    def filter(t: T): Boolean
}
