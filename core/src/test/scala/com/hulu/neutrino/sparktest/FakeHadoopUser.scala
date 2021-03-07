package com.hulu.neutrino.sparktest

import org.apache.hadoop.security.UserGroupInformation
import org.scalatest.{BeforeAndAfterAll, Suite}

trait FakeHadoopUser extends BeforeAndAfterAll {
    this: Suite =>
    override def beforeAll(): Unit = {
        // we need to set fake hadoop user before running spark test cases,
        // otherwise the test cases would fail in docker image.
        // see: https://stackoverflow.com/questions/41864985/hadoop-ioexception-failure-to-login
        UserGroupInformation.setLoginUser(UserGroupInformation.createRemoteUser("hduser"))

        super.beforeAll()
    }
}
