package com.disneystreaming.neutrino.example

trait EventFilter[T] {
    def filter(t: T): Boolean
}
