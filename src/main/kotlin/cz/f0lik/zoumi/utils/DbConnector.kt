package cz.f0lik.zoumi.utils

import java.sql.Connection
import java.sql.DriverManager

class DbConnector {

    fun getConnection(): Connection? {
        Class.forName("org.postgresql.Driver")
        val dbUrl = System.getProperty("dbUrl")
        return DriverManager.getConnection(dbUrl)
    }
}