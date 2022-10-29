package io.github.irgaly.kottage.internal.database

import io.github.irgaly.kottage.KottageEnvironment
import io.github.irgaly.kottage.data.indexeddb.KottageIndexeddbDatabase
import io.github.irgaly.kottage.data.indexeddb.extension.jso
import io.github.irgaly.kottage.data.indexeddb.schema.allStoreSchemaNames
import io.github.irgaly.kottage.data.indexeddb.schema.entity.Item
import io.github.irgaly.kottage.data.indexeddb.schema.entity.Item_event
import io.github.irgaly.kottage.data.indexeddb.schema.entity.Item_stats
import io.github.irgaly.kottage.data.indexeddb.schema.entity.Stats
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
    if (3 <= version) {
        throw error("version 3 以降の indexeddb 初期化処理の実装が必要")
    }
    DatabaseConnection(
        databaseName = "$directoryPath/$fileName",
        dispatcher = dispatcher
    ).transaction {
        with(transaction) {
            objectStore("item_stats").add(
                jso<Item_stats> {
                    item_type = "cache1"
                    count = 1.toDouble()
                    event_count = 1.toDouble()
                }
            )
            objectStore("item_event").add(
                jso<Item_event> {
                    id = "61b658c4250a4e9a9d07a9815655c5e1"
                    created_at = 1640995200000.toDouble()
                    expire_at = 1643587200000.toDouble()
                    item_type = "cache1"
                    item_key = "cache1+key1"
                    event_type = "Create"
                }
            )
            objectStore("stats").add(
                jso<Stats> {
                    key = "kottage"
                    last_evict_at = 1640995200000.toDouble()
                }
            )
            objectStore("item").add(
                jso<Item> {
                    key = "cache1+key1"
                    type = "cache1"
                    string_value = "value1"
                    long_value = null
                    double_value = null
                    bytes_value = null
                    created_at = 1640995200000.toDouble()
                    last_read_at = 1640995200000.toDouble()
                    expire_at = 1643587200000.toDouble()
                }
            )
        }
    }
}
