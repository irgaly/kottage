package io.github.irgaly.kottage.internal.database

import io.github.irgaly.kottage.KottageEnvironment
import io.github.irgaly.kottage.data.indexeddb.extension.jso
import io.github.irgaly.kottage.data.indexeddb.schema.entity.Item
import io.github.irgaly.kottage.data.indexeddb.schema.entity.Item_event
import io.github.irgaly.kottage.data.indexeddb.schema.entity.Item_stats
import io.github.irgaly.kottage.data.indexeddb.schema.entity.Stats
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

internal class IndexeddbDatabaseConnectionFactory: DatabaseConnectionFactory {
    override fun createDatabaseConnection(
        fileName: String,
        directoryPath: String,
        environment: KottageEnvironment,
        scope: CoroutineScope,
        dispatcher: CoroutineDispatcher
    ): IndexeddbDatabaseConnection {
        return IndexeddbDatabaseConnection(
            databaseName = "$directoryPath/$fileName",
            scope = scope,
            dispatcher = dispatcher
        )
    }

    override suspend fun createOldDatabase(
        fileName: String,
        directoryPath: String,
        environment: KottageEnvironment,
        version: Int,
        scope: CoroutineScope,
        dispatcher: CoroutineDispatcher
    ) {
        IndexeddbDatabaseConnection(
            databaseName = "$directoryPath/$fileName",
            scope = scope,
            dispatcher = dispatcher
        ).transaction {
            with((this as IndexeddbTransaction).transaction) {
                if (version <= 4) {
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
                } else if (version <= 5) {
                    objectStore("item_stats").add(
                        jso<Item_stats> {
                            item_type = "cache1"
                            count = 1.toDouble()
                            event_count = 1.toDouble()
                            byte_size = 6.toDouble()
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
                } else {
                    error("version 6 以降の indexeddb 初期化処理の実装が必要")
                }
            }
        }
    }
}
