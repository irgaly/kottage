package io.github.irgaly.kottage.data.indexeddb

import com.juul.indexeddb.Database
import com.juul.indexeddb.openDatabase
import io.github.irgaly.kottage.data.indexeddb.schema.ItemEventStoreSchema
import io.github.irgaly.kottage.data.indexeddb.schema.ItemListStatsStoreSchema
import io.github.irgaly.kottage.data.indexeddb.schema.ItemListStoreSchema
import io.github.irgaly.kottage.data.indexeddb.schema.ItemStatsStoreSchema
import io.github.irgaly.kottage.data.indexeddb.schema.ItemStoreSchema
import io.github.irgaly.kottage.data.indexeddb.schema.StatsStoreSchema

class KottageIndexeddbDatabase(
    val database: Database
) {
    companion object {
        suspend fun open(name: String): KottageIndexeddbDatabase {
            val database = openDatabase(name, 2) { database, oldVersion, newVersion ->
                with(ItemStoreSchema()) { migrate(database, oldVersion, newVersion) }
                with(ItemEventStoreSchema()) { migrate(database, oldVersion, newVersion) }
                with(ItemListStoreSchema()) { migrate(database, oldVersion, newVersion) }
                with(ItemListStatsStoreSchema()) { migrate(database, oldVersion, newVersion) }
                with(ItemStatsStoreSchema()) { migrate(database, oldVersion, newVersion) }
                with(StatsStoreSchema()) { migrate(database, oldVersion, newVersion) }
            }
            return KottageIndexeddbDatabase(database)
        }
    }
}
