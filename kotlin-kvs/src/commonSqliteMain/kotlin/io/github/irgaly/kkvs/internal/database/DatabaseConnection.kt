package io.github.irgaly.kkvs.internal.database

import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.db.use
import io.github.irgaly.kkvs.KkvsEnvironment
import io.github.irgaly.kkvs.data.sqlite.DriverFactory
import io.github.irgaly.kkvs.data.sqlite.Item_event
import io.github.irgaly.kkvs.data.sqlite.KkvsDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
internal actual data class DatabaseConnection(
    val sqlDriver: SqlDriver,
    val database: KkvsDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    actual suspend fun <R> transactionWithResult(bodyWithReturn: () -> R): R =
        withContext(dispatcher) {
            // SQLDelight Transaction = DEFERRED (default)
            database.transactionWithResult {
                // restart transaction with EXCLUSIVE
                sqlDriver.execute(null, "END", 0)
                sqlDriver.execute(null, "BEGIN EXCLUSIVE", 0)
                bodyWithReturn()
            }
        }

    actual suspend fun transaction(body: () -> Unit) = withContext(dispatcher) {
        // SQLDelight Transaction = DEFERRED (default)
        database.transaction {
            // restart transaction with EXCLUSIVE
            sqlDriver.execute(null, "END", 0)
            sqlDriver.execute(null, "BEGIN EXCLUSIVE", 0)
            body()
        }
    }

    actual suspend fun deleteAll() = withContext(dispatcher) {
        database.transaction {
            database.itemQueries.deleteAll()
        }
    }

    actual suspend fun getDatabaseStatus(): String = withContext(dispatcher) {
        database.transactionWithResult {
            //val userVersion = database.pragmaQueries.getUserVersion()
            //val journalMode = database.pragmaQueries.getJournalMode()
            //val synchronous = database.pragmaQueries.getSynchronous()
            //val autoVacuum = database.pragmaQueries.getAutoVacuum()
            //val lockingMode = database.pragmaQueries.getLockingMode()
            val userVersion = sqlDriver.executeQuery(null, "PRAGMA user_version", 0).use {
                if (it.next()) {
                    it.getLong(0)
                } else null
            }
            // Good: WAL + synchronous = NORMAL(1)
            val journalMode = sqlDriver.executeQuery(null, "PRAGMA journal_mode", 0).use {
                if (it.next()) {
                    it.getString(0)
                } else null
            }
            val synchronous = sqlDriver.executeQuery(null, "PRAGMA synchronous", 0).use {
                if (it.next()) {
                    it.getLong(0)
                } else null
            }
            val autoVacuum = sqlDriver.executeQuery(null, "PRAGMA auto_vacuum", 0).use {
                if (it.next()) {
                    it.getLong(0)
                } else null
            }
            val lockingMode = sqlDriver.executeQuery(null, "PRAGMA locking_mode", 0).use {
                if (it.next()) {
                    it.getString(0)
                } else null
            }
            val busyTimeout = sqlDriver.executeQuery(null, "PRAGMA busy_timeout", 0).use {
                if (it.next()) {
                    it.getLong(0)
                } else null
            }
            """
                user_version = $userVersion
                journal_mode = $journalMode
                synchronous = $synchronous
                auto_vacuum = $autoVacuum
                locking_mode = $lockingMode
                busy_timeout = $busyTimeout
            """.trimIndent()
        }
    }
}

internal actual fun createDatabaseConnection(
    fileName: String,
    directoryPath: String,
    environment: KkvsEnvironment,
    dispatcher: CoroutineDispatcher
): DatabaseConnection {
    val driver = DriverFactory(environment.context).createDriver(fileName, directoryPath)
    val database = KkvsDatabase(driver, Item_event.Adapter(EnumColumnAdapter()))
    return DatabaseConnection(driver, database, dispatcher)
}
