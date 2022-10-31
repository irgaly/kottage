package io.github.irgaly.kottage.data.indexeddb.schema

import com.juul.indexeddb.Database
import com.juul.indexeddb.KeyPath
import com.juul.indexeddb.VersionChangeTransaction

class ItemListStatsStoreSchema: StoreSchema {
    override fun VersionChangeTransaction.migrate(database: Database, oldVersion: Int, newVersion: Int) {
        val store = if (oldVersion < 3) {
            database.createObjectStore("item_list_stats", KeyPath("item_list_type"))
        } else {
            objectStore("item_list_stats")
        }
        if (oldVersion < 3) {
            // no index
        }
    }
}
