package io.github.irgaly.kottage.data.indexeddb.schema

import com.juul.indexeddb.Database
import com.juul.indexeddb.KeyPath
import com.juul.indexeddb.VersionChangeTransaction

class StatsStoreSchema: StoreSchema {
    override fun VersionChangeTransaction.migrate(database: Database, oldVersion: Int, newVersion: Int) {
        val store = if (oldVersion < 2) {
            database.createObjectStore("stats", KeyPath("key"))
        } else {
            objectStore("stats")
        }
        if (oldVersion < 2) {
            // no index
        }
    }
}
