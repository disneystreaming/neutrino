package com.hulu.neutrino.example

import java.sql.{Connection, DriverManager}
import javax.inject.{Inject, Provider}

case class DbConfig(url: String, userName: String, password: String)

class DbConnectionProvider @Inject()(dbConfig: DbConfig) extends Provider[java.sql.Connection] {
    override def get(): Connection = {
        DriverManager.getConnection(
            dbConfig.url,
            dbConfig.userName,
            dbConfig.password);
    }
}
