package io.github.irgaly.kottage.data.indexeddb.schema

import com.juul.indexeddb.Database
import com.juul.indexeddb.VersionChangeTransaction

interface StoreSchema {
    fun VersionChangeTransaction.migrate(database: Database, oldVersion: Int, newVersion: Int)
}

fun allStoreSchemaNames(): Array<String> {
    return arrayOf(
        "item",
        "item_event",
        "item_list",
        "item_list_stats",
        "item_stats",
        "stats",
    )
}
