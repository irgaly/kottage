package io.github.irgaly.kottage.internal.database

import io.github.irgaly.kottage.data.indexeddb.KottageIndexeddbDatabase
import io.github.irgaly.kottage.data.indexeddb.schema.allStoreSchemaNames
import io.github.irgaly.kottage.platform.Files
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

internal class IndexeddbDatabaseConnection(
    private val databaseName: String,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : DatabaseConnection {
    @OptIn(DelicateCoroutinesApi::class)
    val database = GlobalScope.async(dispatcher, CoroutineStart.LAZY) {
        KottageIndexeddbDatabase.open(databaseName)
    }

    override suspend fun <R> transactionWithResult(bodyWithReturn: suspend Transaction.() -> R): R {
        val database = database.await()
        return database.database.writeTransaction(*allStoreSchemaNames()) {
            with(IndexeddbTransaction(this)) {
                bodyWithReturn()
            }
        }
    }

    override suspend fun transaction(body: suspend Transaction.() -> Unit) {
        val database = database.await()
        database.database.writeTransaction(*allStoreSchemaNames()) {
            with(IndexeddbTransaction(this)) {
                body()
            }
        }
    }

    override suspend fun deleteAll() {
        TODO("Not yet implemented")
    }

    override suspend fun getDatabaseStatus(): String {
        return "no status for indexeddb"
    }

    override suspend fun backupTo(file: String, directoryPath: String) {
        require(!file.contains(Files.separator)) { "file contains separator: $file" }
        console.warn("backupTo() is not supported with indexeddb\n")
    }

    override suspend fun compact() {
        // indexeddb is managed by browser, no need to maintain from application
    }
}
