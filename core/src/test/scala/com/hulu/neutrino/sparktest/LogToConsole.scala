package com.hulu.neutrino.sparktest

import org.apache.log4j.BasicConfigurator
import org.scalatest.{BeforeAndAfterAll, Suite}

trait LogToConsole extends BeforeAndAfterAll { this: Suite =>
    override def beforeAll(): Unit = {
        // skip log4j default initialization or log4j would throw exception
        System.getProperties.setProperty("log4j.defaultInitOverride", "true")
        System.getProperties.setProperty("org.slf4j.simpleLogger.logFile", "System.out")
        System.getProperties.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn")
        BasicConfigurator.configure()
        this.getClass.getClassLoader.loadClass("org.slf4j.impl.SimpleLogger")
        super.beforeAll()
    }
}
