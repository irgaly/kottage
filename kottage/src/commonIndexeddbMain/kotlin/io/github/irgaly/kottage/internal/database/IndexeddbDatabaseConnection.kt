package io.github.irgaly.kottage.internal.database

import io.github.irgaly.kottage.data.indexeddb.KottageIndexeddbDatabase
import io.github.irgaly.kottage.data.indexeddb.schema.allStoreSchemaNames
import io.github.irgaly.kottage.platform.Files
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class IndexeddbDatabaseConnection(
    private val databaseName: String,
    scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : DatabaseConnection, CoroutineScope by scope {
    private val mutex = Mutex()

    val database = async(dispatcher, CoroutineStart.LAZY) {
        KottageIndexeddbDatabase.open(databaseName)
    }

    override var closed: Boolean = false
        private set

    override suspend fun <R> transactionWithResult(bodyWithReturn: suspend Transaction.() -> R): R {
        return mutex.withLock {
            if (closed) {
                error("Database connection is closed")
            }
            val database = database.await()
            database.database.writeTransaction(*allStoreSchemaNames()) {
                with(IndexeddbTransaction(this)) {
                    bodyWithReturn()
                }
            }
        }
    }

    override suspend fun transaction(body: suspend Transaction.() -> Unit) {
        mutex.withLock {
            if (closed) {
                error("Database connection is closed")
            }
            val database = database.await()
            database.database.writeTransaction(*allStoreSchemaNames()) {
                with(IndexeddbTransaction(this)) {
                    body()
                }
            }
        }
    }

    override suspend fun deleteAll() {
        mutex.withLock {
            if (closed) {
                error("Database connection is closed")
            }
            val database = database.await()
            database.database.writeTransaction(*allStoreSchemaNames()) {
                allStoreSchemaNames().forEach {
                    objectStore(it).clear()
                }
            }
        }
    }

    override suspend fun getDatabaseStatus(): String {
        return mutex.withLock {
            if (closed) {
                error("Database connection is closed")
            }
            "no status for indexeddb"
        }
    }

    override suspend fun backupTo(file: String, directoryPath: String) {
        mutex.withLock {
            if (closed) {
                error("Database connection is closed")
            }
            require(!file.contains(Files.separator)) { "file contains separator: $file" }
            console.warn("backupTo() is not supported with indexeddb\n")
        }
    }

    override suspend fun compact() {
        mutex.withLock {
            if (closed) {
                error("Database connection is closed")
            }
            // indexeddb is managed by browser, no need to maintain from application
        }
    }

    override suspend fun close() {
        mutex.withLock {
            if (!closed) {
                val database = database.await()
                database.database.close()
                closed = true
            }
        }
    }
}
