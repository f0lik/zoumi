package cz.f0lik.zoumi.utils

import java.sql.Connection
import java.sql.DriverManager

class DbConnector private constructor() {
    private var connector: Connection? = null

    companion object {
        private val mInstance: DbConnector = DbConnector()

        @Synchronized
        fun getInstance(): DbConnector {
            return mInstance
        }
    }

    fun getConnection(): Connection? {
        if (connector == null) {
            Class.forName("org.postgresql.Driver")
            val dbUrl = System.getProperty("dbUrl")
            connector = DriverManager.getConnection(dbUrl)
        }
        return connector
    }
}