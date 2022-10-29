package io.github.irgaly.kottage.internal.repository

import com.juul.indexeddb.Key
import com.juul.indexeddb.ObjectStore
import com.juul.indexeddb.WriteTransaction
import com.juul.indexeddb.bound
import com.juul.indexeddb.upperBound
import io.github.irgaly.kottage.data.indexeddb.extension.exists
import io.github.irgaly.kottage.data.indexeddb.extension.jso
import io.github.irgaly.kottage.data.indexeddb.schema.entity.Item_stats
import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.extension.take
import io.github.irgaly.kottage.internal.model.Item
import io.github.irgaly.kottage.internal.model.ItemStats
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

internal class KottageIndexeddbItemRepository : KottageItemRepository {
    override suspend fun upsert(transaction: Transaction, item: Item) {
        transaction.store { store ->
            store.put(item.toEntity())
        }
    }

    override suspend fun updateLastRead(
        transaction: Transaction,
        key: String,
        itemType: String,
        lastReadAt: Long
    ) {
        transaction.store { store ->
            store.get(Key(Item.toEntityKey(key, itemType)))
                ?.unsafeCast<io.github.irgaly.kottage.data.indexeddb.schema.entity.Item>()
                ?.let { item ->
                    item.last_read_at = lastReadAt.toDouble()
                    store.put(item)
                }
        }
    }

    override suspend fun updateExpireAt(
        transaction: Transaction,
        key: String,
        itemType: String,
        expireAt: Long
    ) {
        transaction.store { store ->
            store.get(Key(Item.toEntityKey(key, itemType)))
                ?.unsafeCast<io.github.irgaly.kottage.data.indexeddb.schema.entity.Item>()
                ?.let { item ->
                    item.expire_at = expireAt.toDouble()
                    store.put(item)
                }
        }
    }

    override suspend fun exists(transaction: Transaction, key: String, itemType: String): Boolean {
        return transaction.store { store ->
            exists(store, Key(Item.toEntityKey(key, itemType)))
        }
    }

    override suspend fun get(transaction: Transaction, key: String, itemType: String): Item? {
        return transaction.store { store ->
            store.get(Key(Item.toEntityKey(key, itemType)))
                ?.unsafeCast<io.github.irgaly.kottage.data.indexeddb.schema.entity.Item>()
                ?.toDomain()
        }
    }

    override suspend fun getCount(transaction: Transaction, itemType: String): Long {
        return transaction.store { store ->
            store.index("item_type").count(Key(itemType)).toLong()
        }
    }

    override suspend fun getAllKeys(
        transaction: Transaction,
        itemType: String,
        receiver: suspend (key: String) -> Unit
    ) {
        transaction.store { store ->
            store.index("item_type_created_at").openKeyCursor(
                // type = itemType && created_at = Any
                bound(
                    arrayOf(itemType),
                    arrayOf(itemType, emptyArray<Any>())
                )
            ).collect { cursor ->
                receiver(cursor.primaryKey.unsafeCast<String>())
            }
        }
    }

    override suspend fun getExpiredKeys(
        transaction: Transaction,
        now: Long,
        itemType: String?,
        receiver: suspend (key: String, itemType: String) -> Unit
    ) {
        transaction.store { store ->
            if (itemType != null) {
                store.index("item_type_expire_at").openKeyCursor(
                    // type = itemType && created_at <= now
                    bound(
                        arrayOf(itemType),
                        arrayOf(itemType, now.toDouble())
                    )
                ).collect { cursor ->
                    val key = cursor.primaryKey.unsafeCast<String>()
                    receiver(Item.keyFromEntityKey(key, itemType), itemType)
                }
            } else {
                store.index("item_expire_at").openCursor(
                    // created_at <= now
                    upperBound(now.toDouble())
                ).collect { cursor ->
                    val item =
                        cursor.value.unsafeCast<io.github.irgaly.kottage.data.indexeddb.schema.entity.Item>()
                    receiver(Item.keyFromEntityKey(item.key, item.type), item.type)
                }
            }
        }
    }

    override suspend fun getLeastRecentlyUsedKeys(
        transaction: Transaction,
        itemType: String,
        limit: Long?,
        receiver: suspend (key: String) -> Boolean
    ) {
        transaction.store { store ->
            store.index("item_type_last_read_at").openKeyCursor(
                // type = itemType
                bound(
                    arrayOf(itemType),
                    arrayOf(itemType, emptyArray<Any>())
                )
            ).let {
                if (limit != null) it.take(limit) else it
            }.collect { cursor ->
                val key = cursor.primaryKey.unsafeCast<String>()
                receiver(Item.keyFromEntityKey(key, itemType))
            }
        }
    }

    override suspend fun getOlderKeys(
        transaction: Transaction,
        itemType: String,
        limit: Long?,
        receiver: suspend (key: String) -> Boolean
    ) {
        transaction.store { store ->
            store.index("item_type_created_at").openKeyCursor(
                // type = itemType
                bound(
                    arrayOf(itemType),
                    arrayOf(itemType, emptyArray<Any>())
                )
            ).let {
                if (limit != null) it.take(limit) else it
            }.collect { cursor ->
                val key = cursor.primaryKey.unsafeCast<String>()
                receiver(Item.keyFromEntityKey(key, itemType))
            }
        }
    }

    override suspend fun getStats(transaction: Transaction, itemType: String): ItemStats? {
        return transaction.statsStore { store ->
            store.get(Key(itemType))?.unsafeCast<Item_stats>()?.toDomain()
        }
    }

    override suspend fun getEmptyStats(transaction: Transaction, limit: Long): List<ItemStats> {
        return transaction.statsStore { store ->
            // SQLite 側に合わせて index なしで総当たり処理
            store.openCursor().map { cursor ->
                cursor.value.unsafeCast<Item_stats>()
            }.filter { stats ->
                ((stats.count.toLong() <= 0L) && (stats.event_count.toLong() <= 0))
            }.take(limit).map {
                it.toDomain()
            }.toList()
        }
    }

    override suspend fun delete(transaction: Transaction, key: String, itemType: String) {
        transaction.store { store ->
            store.delete(Key(Item.toEntityKey(key, itemType)))
        }
    }

    override suspend fun deleteAll(transaction: Transaction, itemType: String) {
        transaction.store { store ->
            store.index("item_type").openCursor(
                // type = itemType
                Key(itemType)
            ).collect { cursor ->
                cursor.delete()
            }
        }
    }

    override suspend fun getStatsCount(transaction: Transaction, itemType: String): Long {
        return transaction.statsStore { store ->
            store.get(Key(itemType))?.unsafeCast<Item_stats>()?.count?.toLong() ?: 0
        }
    }

    override suspend fun incrementStatsCount(
        transaction: Transaction,
        itemType: String,
        count: Long
    ) {
        transaction.statsStore { store ->
            val stats = getOrCreate(store, itemType)
            stats.count += count.toDouble()
            store.put(stats)
        }
    }

    override suspend fun decrementStatsCount(
        transaction: Transaction,
        itemType: String,
        count: Long
    ) {
        transaction.statsStore { store ->
            val stats = getOrCreate(store, itemType)
            stats.count -= count.toDouble()
            store.put(stats)
        }
    }

    override suspend fun updateStatsCount(transaction: Transaction, itemType: String, count: Long) {
        transaction.statsStore { store ->
            val stats = getOrCreate(store, itemType)
            stats.count = count.toDouble()
            store.put(stats)
        }
    }

    override suspend fun deleteStats(transaction: Transaction, itemType: String) {
        transaction.statsStore { store ->
            store.delete(Key(itemType))
        }
    }

    private suspend fun WriteTransaction.getOrCreate(
        store: ObjectStore,
        itemType: String
    ): Item_stats {
        var stats = store.get(Key(itemType))?.unsafeCast<Item_stats>()
        if (stats == null) {
            stats = jso {
                item_type = itemType
                count = 0L.toDouble()
                event_count = 0L.toDouble()
            }
            store.add(stats)
        }
        return stats
    }

    private inline fun <R> Transaction.store(block: WriteTransaction.(store: ObjectStore) -> R): R {
        return with(transaction) { block(transaction.objectStore("item")) }
    }

    private inline fun <R> Transaction.statsStore(block: WriteTransaction.(store: ObjectStore) -> R): R {
        return with(transaction) { block(transaction.objectStore("item_stats")) }
    }
}
