package io.github.irgaly.kkvs.internal.database

import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import io.github.irgaly.kkvs.KkvsEnvironment
import io.github.irgaly.kkvs.data.sqlite.DriverFactory
import io.github.irgaly.kkvs.data.sqlite.Item_event
import io.github.irgaly.kkvs.data.sqlite.KkvsDatabase

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

    actual suspend fun deleteAll() {
        database.transaction {
            database.itemQueries.deleteAll()
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
