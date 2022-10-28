package io.github.irgaly.kottage.data.indexeddb.schema

import com.juul.indexeddb.Database
import com.juul.indexeddb.KeyPath
import com.juul.indexeddb.VersionChangeTransaction

class ItemStoreSchema: StoreSchema {
    override fun VersionChangeTransaction.migrate(database: Database, oldVersion: Int, newVersion: Int) {
        val store = if (oldVersion < 3) {
            database.createObjectStore("item", KeyPath("key"))
        } else {
            objectStore("item")
        }
        if (oldVersion < 3) {
            store.createIndex("item_type", KeyPath("type"), false)
            store.createIndex("item_type_created_at", KeyPath("type", "created_at"), false)
            store.createIndex("item_type_last_read_at", KeyPath("type", "last_read_at"), false)
            store.createIndex("item_type_expire_at", KeyPath("type", "expire_at"), false)
            store.createIndex("item_expire_at", KeyPath("expire_at"), false)
        }
    }
}
