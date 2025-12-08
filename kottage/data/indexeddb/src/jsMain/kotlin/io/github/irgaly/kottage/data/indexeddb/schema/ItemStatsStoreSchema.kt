package io.github.irgaly.kottage.data.indexeddb.schema

import com.juul.indexeddb.Database
import com.juul.indexeddb.KeyPath
import com.juul.indexeddb.VersionChangeTransaction
import io.github.irgaly.kottage.data.indexeddb.schema.entity.Item_stats

class ItemStatsStoreSchema: StoreSchema {
    override suspend fun VersionChangeTransaction.migrate(
        database: Database,
        oldVersion: Int,
        newVersion: Int
    ) {
        val store = if (oldVersion < 3) {
            database.createObjectStore("item_stats", KeyPath("item_type"))
        } else {
            objectStore("item_stats")
        }
        if (oldVersion < 3) {
            // no index
        }
        if (oldVersion < 5) {
            // migrate 4 -> 5
            store.openCursor(autoContinue = false).collect { cursor ->
                val itemStats = cursor.value.unsafeCast<Item_stats>()
                itemStats.byte_size = 0.toDouble()
                cursor.update(itemStats)
                cursor.`continue`()
            }
        }
    }
}
