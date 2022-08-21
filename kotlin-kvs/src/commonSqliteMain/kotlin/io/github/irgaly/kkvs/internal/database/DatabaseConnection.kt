package io.github.irgaly.kkvs.internal.database

import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.db.use
import io.github.irgaly.kkvs.KkvsEnvironment
import io.github.irgaly.kkvs.data.sqlite.DriverFactory
import io.github.irgaly.kkvs.data.sqlite.Item_event
import io.github.irgaly.kkvs.data.sqlite.KkvsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

internal actual data class DatabaseConnection(
    val sqlDriver: SqlDriver,
    val database: KkvsDatabase
) {
    actual suspend fun <R> transactionWithResult(bodyWithReturn: suspend () -> R): R =
        withContext(Dispatchers.Default) {
            database.transactionWithResult {
                runBlocking {
                    bodyWithReturn()
                }
            }
        }

    actual suspend fun transaction(body: suspend () -> Unit) = withContext(Dispatchers.Default) {
        database.transaction {
            runBlocking {
                body()
            }
        }
    }

    actual suspend fun deleteAll() = withContext(Dispatchers.Default) {
        database.transaction {
            database.itemQueries.deleteAll()
        }
    }

    actual suspend fun getDatabaseStatus(): String = withContext(Dispatchers.Default) {
        database.transactionWithResult {
            //val userVersion = database.pragmaQueries.getUserVersion()
            //val journalMode = database.pragmaQueries.getJournalMode()
            //val synchronous = database.pragmaQueries.getSynchronous()
            //val autoVacuum = database.pragmaQueries.getAutoVacuum()
            //val lockingMode = database.pragmaQueries.getLockingMode()
            val userVersion = sqlDriver.executeQuery(null, "PRAGMA user_version", 0, null).use {
                if (it.next()) {
                    it.getLong(0)
                } else null
            }
            // Good: WAL + synchronous = NORMAL(1)
            val journalMode = sqlDriver.executeQuery(null, "PRAGMA journal_mode", 0, null).use {
                if (it.next()) {
                    it.getString(0)
                } else null
            }
            val synchronous = sqlDriver.executeQuery(null, "PRAGMA synchronous", 0, null).use {
                if (it.next()) {
                    it.getLong(0)
                } else null
            }
            val autoVacuum = sqlDriver.executeQuery(null, "PRAGMA auto_vacuum", 0, null).use {
                if (it.next()) {
                    it.getLong(0)
                } else null
            }
            val lockingMode = sqlDriver.executeQuery(null, "PRAGMA locking_mode", 0, null).use {
                if (it.next()) {
                    it.getString(0)
                } else null
            }
            """
                user_version = $userVersion
                journal_mode = $journalMode
                synchronous = $synchronous
                auto_vacuum = $autoVacuum
                locking_mode = $lockingMode
            """.trimIndent()
        }
    }
}

internal actual fun createDatabaseConnection(
    fileName: String,
    directoryPath: String,
    environment: KkvsEnvironment
): DatabaseConnection {
    val driver = DriverFactory(environment.context).createDriver(fileName, directoryPath)
    val database = KkvsDatabase(driver, Item_event.Adapter(EnumColumnAdapter()))
    return DatabaseConnection(driver, database)
}
