package io.github.irgaly.kottage.data.indexeddb.schema

import com.juul.indexeddb.Database
import com.juul.indexeddb.KeyPath
import com.juul.indexeddb.VersionChangeTransaction

class ItemEventStoreSchema: StoreSchema {
    override fun VersionChangeTransaction.migrate(database: Database, oldVersion: Int, newVersion: Int) {
        val store = if (oldVersion < 2) {
            database.createObjectStore("item_event", KeyPath("id"))
        } else {
            objectStore("item_event")
        }
        if (oldVersion < 2) {
            store.createIndex("item_event_created_at", KeyPath("created_at"), false)
            store.createIndex("item_event_expire_at", KeyPath("expire_at"), false)
            store.createIndex("item_event_item_type_created_at", KeyPath("item_type", "created_at"), false)
            store.createIndex("item_event_item_type_expire_at", KeyPath("item_type", "expire_at"), false)
            store.createIndex("item_event_item_list_type_created_at", KeyPath("item_list_type", "created_at"), false)
            store.createIndex("item_event_item_list_type_expire_at", KeyPath("item_list_type", "expire_at"), false)
        }
    }
}
