package io.github.irgaly.kottage.data.indexeddb.schema

import com.juul.indexeddb.Database
import com.juul.indexeddb.KeyPath
import com.juul.indexeddb.VersionChangeTransaction

class StatsStoreSchema: StoreSchema {
    override suspend fun VersionChangeTransaction.migrate(
        database: Database,
        oldVersion: Int,
        newVersion: Int
    ) {
        val store = if (oldVersion < 3) {
            database.createObjectStore("stats", KeyPath("key"))
        } else {
            objectStore("stats")
        }
        if (oldVersion < 3) {
            // no index
        }
    }
}
