package com.hulu.neutrino.example

import javax.inject.Inject
import scala.collection.mutable

class DbUserWhiteListsEventFilter @Inject()(dbConnection: java.sql.Connection) extends EventFilter[TestEvent] {
    private lazy val userIdSet = {
        val statement = dbConnection.createStatement()
        try {
            val userIdSet = mutable.Set[String]()
            val resultSet = statement.executeQuery("select userId from ValidUser")
            while (resultSet.next()) {
                userIdSet.add(resultSet.getString(0))
            }

            userIdSet
        } finally {
            statement.close()
        }
    }

    override def filter(t: TestEvent): Boolean = {
        t.userId != null && userIdSet.contains(t.userId)
    }
}
