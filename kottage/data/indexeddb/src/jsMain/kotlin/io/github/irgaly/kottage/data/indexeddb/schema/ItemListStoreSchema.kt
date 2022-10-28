package io.github.irgaly.kottage.data.indexeddb.schema

import com.juul.indexeddb.Database
import com.juul.indexeddb.KeyPath
import com.juul.indexeddb.VersionChangeTransaction

class ItemListStoreSchema: StoreSchema {
    override fun VersionChangeTransaction.migrate(database: Database, oldVersion: Int, newVersion: Int) {
        val store = if (oldVersion < 3) {
            database.createObjectStore("item_list", KeyPath("id"))
        } else {
            objectStore("item_list")
        }
        if (oldVersion < 3) {
            store.createIndex("item_list_item_type", KeyPath("item_type"), false)
            store.createIndex(
                "item_list_item_type_item_key",
                KeyPath("item_type", "item_key"),
                false
            )
            store.createIndex(
                "item_list_type_item_type_expire_at",
                KeyPath("type", "item_type", "expire_at"),
                false
            )
        }
    }
}
