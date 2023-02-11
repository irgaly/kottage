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
import io.github.irgaly.kottage.internal.database.IndexeddbTransaction
import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.extension.deleteWithChunk
import io.github.irgaly.kottage.internal.extension.take
import io.github.irgaly.kottage.internal.model.ItemEvent
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

internal class KottageIndexeddbItemEventRepository : KottageItemEventRepository {
    override suspend fun create(transaction: Transaction, itemEvent: ItemEvent) {
        transaction.store { store ->
            store.add(itemEvent.toIndexeddbEntity())
        }
    }

    override suspend fun selectAfter(
        transaction: Transaction,
        createdAt: Long,
        limit: Long?
    ): List<ItemEvent> {
        return transaction.store { store ->
            store.index("item_event_created_at")
                .openCursor(
                    // createdAt < created_at
                    lowerBound(createdAt.toDouble(), true),
                    autoContinue = true
                ).let {
                    if (limit != null) it.take(limit) else it
                }.map { cursor ->
                    cursor.value.unsafeCast<Item_event>().toDomain()
                }.toList()
        }
    }

    override suspend fun selectItemEventAfter(
        transaction: Transaction,
        itemType: String,
        createdAt: Long,
        limit: Long?
    ): List<ItemEvent> {
        return transaction.store { store ->
            store.index("item_event_item_type_created_at")
                .openCursor(
                    // item_type = itemType && createdAt < created_at
                    bound(
                        arrayOf(itemType, createdAt.toDouble()),
                        arrayOf(itemType, emptyArray<Any>()),
                        lowerOpen = true
                    ),
                    autoContinue = true
                ).map { cursor ->
                    cursor.value.unsafeCast<Item_event>().toDomain()
                }.filter {
                    // indexeddb は index 項目に null を含められないため
                    // index ではなく filter で除外
                    (it.itemListType == null)
                }.let {
                    if (limit != null) it.take(limit) else it
                }.toList()
        }
    }

    override suspend fun selectListEventAfter(
        transaction: Transaction,
        listType: String,
        createdAt: Long,
        limit: Long?
    ): List<ItemEvent> {
        return transaction.store { store ->
            store.index("item_event_item_list_type_created_at")
                .openCursor(
                    // item_list_type = listType && createdAt < created_at
                    bound(
                        arrayOf(listType, createdAt.toDouble()),
                        arrayOf(listType, emptyArray<Any>()),
                        lowerOpen = true
                    ),
                    autoContinue = true
                ).let {
                    if (limit != null) it.take(limit) else it
                }.map { cursor ->
                    cursor.value.unsafeCast<Item_event>().toDomain()
                }.toList()
        }
    }

    override suspend fun getLatestCreatedAt(transaction: Transaction): Long? {
        return transaction.store { store ->
            store.index("item_event_created_at")
                .openCursor(
                    // created_at DESC
                    direction = Cursor.Direction.Previous
                ).map { cursor ->
                    cursor.value.unsafeCast<Item_event>().created_at.toLong()
                }.firstOrNull()
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

    override suspend fun deleteExpiredEvents(
        transaction: Transaction,
        now: Long,
        itemType: String?,
        onDelete: (suspend (id: String, itemType: String) -> Unit)?
    ): Long {
        return transaction.store { store ->
            if (itemType != null) {
                store.index("item_event_item_type_expire_at")
                    .deleteWithChunk<Item_event, String>(
                        this,
                        store = store,
                        query = bound(
                            // item_type = itemType && expire_at <= now
                            arrayOf(itemType),
                            arrayOf(itemType, now.toDouble())
                        ),
                        chunkSize = 100L,
                        primaryKey = { it.id }
                    ) {
                        onDelete?.invoke(it.id, it.item_type)
                    }
            } else {
                store.index("item_event_expire_at")
                    .deleteWithChunk<Item_event, String>(
                        this,
                        store = store,
                        query = upperBound(now.toDouble()), // expire_at <= now
                        chunkSize = 100L,
                        primaryKey = { it.id }
                    ) {
                        onDelete?.invoke(it.id, it.item_type)
                    }
            }
        }
    }

    override suspend fun deleteOlderEvents(
        transaction: Transaction,
        itemType: String,
        limit: Long
    ) {
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
                upperBound(createdAt.toDouble(), true)
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
            store.get(Key(itemType))?.unsafeCast<Item_stats>()?.event_count?.toLong() ?: 0L
        }
    }

    override suspend fun incrementStatsCount(
        transaction: Transaction,
        itemType: String,
        count: Long
    ) {
        transaction.statsStore { store ->
            val stats = getOrCreate(store, itemType)
            stats.event_count += count.toDouble()
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
            stats.event_count -= count.toDouble()
            store.put(stats)
        }
    }

    override suspend fun updateStatsCount(
        transaction: Transaction,
        itemType: String,
        count: Long
    ) {
        transaction.statsStore { store ->
            val stats = getOrCreate(store, itemType)
            stats.event_count = count.toDouble()
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
                count = 0L.toDouble()
                event_count = 0L.toDouble()
            }
            store.add(stats)
        }
        return stats
    }

    private inline fun <R> Transaction.store(block: WriteTransaction.(store: ObjectStore) -> R): R {
        return with((this as IndexeddbTransaction).transaction) { block(transaction.objectStore("item_event")) }
    }

    private inline fun <R> Transaction.statsStore(block: WriteTransaction.(store: ObjectStore) -> R): R {
        return with((this as IndexeddbTransaction).transaction) { block(transaction.objectStore("item_stats")) }
    }
}
