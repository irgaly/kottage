package io.github.irgaly.kottage.internal.repository

import com.juul.indexeddb.Cursor
import com.juul.indexeddb.Key
import com.juul.indexeddb.ObjectStore
import com.juul.indexeddb.WriteTransaction
import com.juul.indexeddb.bound
import com.juul.indexeddb.lowerBound
import com.juul.indexeddb.upperBound
import io.github.irgaly.kottage.data.indexeddb.extension.jso
import io.github.irgaly.kottage.data.indexeddb.schema.entity.Item_event
import io.github.irgaly.kottage.data.indexeddb.schema.entity.Item_stats
import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.extension.take
import io.github.irgaly.kottage.internal.model.ItemEvent
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

internal class KottageIndexeddbItemEventRepository : KottageItemEventRepository {
    override suspend fun create(transaction: Transaction, itemEvent: ItemEvent) {
        transaction.store { store ->
            store.add(itemEvent.toEntity())
        }
    }

    override suspend fun selectAfter(
        transaction: Transaction,
        createdAt: Long,
        itemType: String?,
        limit: Long?
    ): List<ItemEvent> {
        return transaction.store { store ->
            (if (itemType != null) {
                store.index("item_event_item_type_created_at")
                    .openCursor(
                        // item_type = itemType && createdAt < created_at
                        bound(
                            arrayOf(itemType, createdAt),
                            arrayOf(itemType, emptyArray<Any>()),
                            lowerOpen = true
                        )
                    )
            } else {
                store.index("item_event_created_at")
                    .openCursor(
                        // createdAt < created_at
                        lowerBound(createdAt, true)
                    )
            }).let {
                if (limit != null) it.take(limit) else it
            }.map { cursor ->
                cursor.value.unsafeCast<Item_event>().toDomain()
            }.toList()
        }
    }

    override suspend fun getLatestCreatedAt(transaction: Transaction, itemType: String): Long? {
        return transaction.store { store ->
            store.index("item_event_item_type_created_at")
                .openCursor(
                    // item_type = itemType && created_at DESC
                    bound(
                        arrayOf(itemType),
                        arrayOf(itemType, emptyArray<Any>())
                    ),
                    Cursor.Direction.Previous
                ).map { cursor ->
                    cursor.value.unsafeCast<Item_event>().created_at
                }.firstOrNull()
        }
    }

    override suspend fun getExpiredIds(
        transaction: Transaction,
        now: Long,
        itemType: String?,
        receiver: suspend (id: String, itemType: String) -> Unit
    ) {
        transaction.store { store ->
            if (itemType != null) {
                store.index("item_event_item_type_expire_at").openKeyCursor(
                    // item_type = itemType && expire_at <= now
                    bound(
                        arrayOf(itemType),
                        arrayOf(itemType, now)
                    )
                ).collect { cursor ->
                    val id = cursor.primaryKey.unsafeCast<String>()
                    receiver(id, itemType)
                }
            } else {
                store.index("item_event_expire_at").openCursor(
                    // expire_at <= now
                    upperBound(now)
                ).collect { cursor ->
                    val event =
                        cursor.value.unsafeCast<Item_event>()
                    receiver(event.id, event.item_type)
                }
            }
        }
    }

    override suspend fun getCount(transaction: Transaction, itemType: String): Long {
        return transaction.store { store ->
            store.index("item_event_item_type_created_at").count(
                // item_type = itemType
                bound(
                    arrayOf(itemType),
                    arrayOf(itemType, emptyArray<Any>())
                )
            ).toLong()
        }
    }

    override suspend fun delete(transaction: Transaction, id: String) {
        transaction.store { store ->
            store.delete(Key(id))
        }
    }

    override suspend fun deleteOlderEvents(transaction: Transaction, itemType: String, limit: Long) {
        transaction.store { store ->
            store.index("item_event_item_type_created_at").openCursor(
                // item_type = itemType
                bound(
                    arrayOf(itemType),
                    arrayOf(itemType, emptyArray<Any>())
                )
            ).take(limit).collect { cursor ->
                cursor.delete()
            }
        }
    }

    override suspend fun deleteBefore(transaction: Transaction, createdAt: Long) {
        transaction.store { store ->
            store.index("item_event_created_at").openCursor(
                // created_at < createdAt
                upperBound(createdAt, true)
            ).collect { cursor ->
                cursor.delete()
            }
        }
    }

    override suspend fun deleteAll(transaction: Transaction, itemType: String) {
        transaction.store { store ->
            store.index("item_event_created_at").openCursor(
                // item_type = itemType
                bound(
                    arrayOf(itemType),
                    arrayOf(itemType, emptyArray<Any>())
                )
            ).collect { cursor ->
                cursor.delete()
            }
        }
    }

    override suspend fun deleteAllList(transaction: Transaction, listType: String) {
        transaction.store { store ->
            store.index("item_event_item_list_type_created_at").openCursor(
                // item_list_type = listType
                bound(
                    arrayOf(listType),
                    arrayOf(listType, emptyArray<Any>())
                )
            ).collect { cursor ->
                cursor.delete()
            }
        }
    }

    override suspend fun getStatsCount(transaction: Transaction, itemType: String): Long {
        return transaction.statsStore { store ->
            store.get(Key(itemType))?.unsafeCast<Item_stats>()?.event_count ?: 0L
        }
    }

    override suspend fun incrementStatsCount(transaction: Transaction, itemType: String, count: Long) {
        transaction.statsStore { store ->
            val stats = getOrCreate(store, itemType)
            stats.event_count += count
            store.put(stats)
        }
    }

    override suspend fun decrementStatsCount(transaction: Transaction, itemType: String, count: Long) {
        transaction.statsStore { store ->
            val stats = getOrCreate(store, itemType)
            stats.event_count -= count
            store.put(stats)
        }
    }

    override suspend fun updateStatsCount(transaction: Transaction, itemType: String, count: Long) {
        transaction.statsStore { store ->
            val stats = getOrCreate(store, itemType)
            stats.event_count = count
            store.put(stats)
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
                count = 0
                event_count = 0
            }
            store.add(stats)
        }
        return stats
    }

    private inline fun <R> Transaction.store(block: WriteTransaction.(store: ObjectStore) -> R): R {
        return with(transaction) { block(transaction.objectStore("item_event")) }
    }

    private inline fun <R> Transaction.statsStore(block: WriteTransaction.(store: ObjectStore) -> R): R {
        return with(transaction) { block(transaction.objectStore("item_stats")) }
    }
}
