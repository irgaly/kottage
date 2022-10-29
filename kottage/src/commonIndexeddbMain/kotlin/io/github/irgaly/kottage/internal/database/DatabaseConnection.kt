package io.github.irgaly.kottage.internal.database

import io.github.irgaly.kottage.KottageEnvironment
import io.github.irgaly.kottage.data.indexeddb.KottageIndexeddbDatabase
import io.github.irgaly.kottage.data.indexeddb.schema.allStoreSchemaNames
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

internal actual class DatabaseConnection(
    private val databaseName: String,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    @OptIn(DelicateCoroutinesApi::class)
    val database = GlobalScope.async(dispatcher, CoroutineStart.LAZY) {
        KottageIndexeddbDatabase.open(databaseName)
    }

    actual suspend fun <R> transactionWithResult(bodyWithReturn: suspend Transaction.() -> R): R {
        val database = database.await()
        return database.database.writeTransaction(*allStoreSchemaNames()) {
            with(Transaction(this)) {
                bodyWithReturn()
            }
        }
    }

    actual suspend fun transaction(body: suspend Transaction.() -> Unit) {
        val database = database.await()
        database.database.writeTransaction(*allStoreSchemaNames()) {
            with(Transaction(this)) {
                body()
            }
        }
    }

    actual suspend fun deleteAll() {
        TODO("Not yet implemented")
    }

    actual suspend fun getDatabaseStatus(): String {
        return "no status for indexeddb"
    }

    actual suspend fun backupTo(file: String, directoryPath: String) {
        console.warn("backupTo() is not supported with indexeddb")
    }

    actual suspend fun compact() {
        // indexeddb is managed by browser, no need to maintain from application
    }
}

internal actual fun createDatabaseConnection(
    fileName: String,
    directoryPath: String,
    environment: KottageEnvironment,
    dispatcher: CoroutineDispatcher
): DatabaseConnection {
    return DatabaseConnection(
        databaseName = "$directoryPath/$fileName",
        dispatcher = dispatcher
    )
}

internal actual suspend fun createOldDatabase(
    fileName: String,
    directoryPath: String,
    environment: KottageEnvironment,
    version: Int,
    dispatcher: CoroutineDispatcher
) {
    TODO()
}
