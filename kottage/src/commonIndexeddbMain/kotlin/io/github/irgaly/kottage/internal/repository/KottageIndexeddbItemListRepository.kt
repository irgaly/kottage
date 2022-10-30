package io.github.irgaly.kottage.internal.repository

import com.juul.indexeddb.Key
import com.juul.indexeddb.ObjectStore
import com.juul.indexeddb.WriteTransaction
import com.juul.indexeddb.bound
import com.juul.indexeddb.external.IDBKeyRange.Companion.only
import com.juul.indexeddb.lowerBound
import io.github.irgaly.kottage.data.indexeddb.extension.jso
import io.github.irgaly.kottage.data.indexeddb.schema.entity.Item_list
import io.github.irgaly.kottage.data.indexeddb.schema.entity.Item_list_stats
import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.extension.iterateWithChunk
import io.github.irgaly.kottage.internal.extension.take
import io.github.irgaly.kottage.internal.model.ItemListEntry
import io.github.irgaly.kottage.internal.model.ItemListStats
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

internal class KottageIndexeddbItemListRepository : KottageItemListRepository {
    override suspend fun upsert(transaction: Transaction, entry: ItemListEntry) {
        transaction.store { store ->
            store.put(entry.toEntity())
        }
    }

    override suspend fun updatePreviousId(
        transaction: Transaction,
        id: String,
        previousId: String?
    ) {
        transaction.store { store ->
            store.get(Key(id))
                ?.unsafeCast<Item_list>()
                ?.let { list ->
                    list.previous_id = previousId
                    store.put(list)
                }
        }
    }

    override suspend fun updateNextId(transaction: Transaction, id: String, nextId: String?) {
        transaction.store { store ->
            store.get(Key(id))
                ?.unsafeCast<Item_list>()
                ?.let { list ->
                    list.next_id = nextId
                    store.put(list)
                }
        }
    }

    override suspend fun updateItemKey(
        transaction: Transaction,
        id: String,
        itemType: String,
        itemKey: String?,
        expireAt: Long?
    ) {
        transaction.store { store ->
            store.get(Key(id))
                ?.unsafeCast<Item_list>()
                ?.let { list ->
                    list.item_key = itemKey
                    list.item_type = itemType
                    list.expire_at = expireAt?.toDouble()
                    store.put(list)
                }
        }
    }

    override suspend fun updateExpireAt(transaction: Transaction, id: String, expireAt: Long?) {
        transaction.store { store ->
            store.get(Key(id))
                ?.unsafeCast<Item_list>()
                ?.let { list ->
                    list.expire_at = expireAt?.toDouble()
                    store.put(list)
                }
        }
    }

    override suspend fun removeItemKey(transaction: Transaction, id: String) {
        transaction.store { store ->
            store.get(Key(id))
                ?.unsafeCast<Item_list>()
                ?.let { list ->
                    list.item_key = null
                    store.put(list)
                }
        }
    }

    override suspend fun removeUserData(transaction: Transaction, id: String) {
        transaction.store { store ->
            store.get(Key(id))
                ?.unsafeCast<Item_list>()
                ?.let { list ->
                    list.user_info = null
                    list.user_previous_key = null
                    list.user_current_key = null
                    list.user_next_key = null
                    store.put(list)
                }
        }
    }

    override suspend fun get(transaction: Transaction, id: String): ItemListEntry? {
        return transaction.store { store ->
            store.get(Key(id))
                ?.unsafeCast<Item_list>()
                ?.toDomain()
        }
    }

    override suspend fun getIds(transaction: Transaction, itemType: String, itemKey: String): List<String> {
        return transaction.store { store ->
            store.index("item_list_item_type_item_key").openKeyCursor(
                // item_type = itemType && item_key = itemKey
                Key(only(arrayOf(itemType, itemKey)))
            ).map { cursor ->
                cursor.primaryKey.unsafeCast<String>()
            }.toList()
        }
    }

    override suspend fun getInvalidatedItemIds(
        transaction: Transaction,
        type: String,
        beforeExpireAt: Long?,
        limit: Long
    ): List<String> {
        return transaction.store { store ->
            (if (beforeExpireAt != null) {
                store.index("item_list_type_expire_at").openCursor(
                    // type = type && expire_at < beforeExpireAt
                    bound(
                        arrayOf(type),
                        arrayOf(type, beforeExpireAt.toDouble()),
                        upperOpen = true
                    )
                )
            } else {
                store.index("item_list_type_expire_at").openCursor(
                    // type = type
                    bound(
                        arrayOf(type),
                        arrayOf(type, emptyArray<Any>())
                    )
                )
            }).map { cursor ->
                cursor.value.unsafeCast<Item_list>()
            }.filter {
                (it.item_key == null)
            }.take(limit).map { it.id }.toList()
        }
    }

    override suspend fun getCount(transaction: Transaction, type: String): Long {
        return transaction.store { store ->
            store.index("item_list_type").count(
                Key(type)
            ).toLong()
        }
    }

    override suspend fun getInvalidatedItemCount(transaction: Transaction, type: String): Long {
        return transaction.store { store ->
            store.index("item_list_type").openCursor(
                Key(type)
            ).count { cursor ->
                (cursor.value.unsafeCast<Item_list>().item_key == null)
            }.toLong()
        }
    }

    override suspend fun getAllTypes(transaction: Transaction, receiver: suspend (type: String) -> Unit) {
        transaction.statsStore { store ->
            store.iterateWithChunk<Item_list_stats, String, String>(
                this,
                chunkSize = 100L,
                primaryKey = { it.item_list_type },
                sortKey = { it.item_list_type },
                initialRange = null,
                resumeRange = { lowerBound(it.item_list_type, open = true) }
            ) {
                receiver(it.item_list_type)
                true
            }
        }
    }

    override suspend fun delete(transaction: Transaction, id: String) {
        transaction.store { store ->
            store.delete(Key(id))
        }
    }

    override suspend fun deleteAll(transaction: Transaction, type: String) {
        transaction.store { store ->
            store.index("item_list_type_expire_at").openCursor(
                // type = type
                bound(
                    arrayOf(type),
                    arrayOf(type, emptyArray<Any>())
                )
            ).collect { cursor ->
                cursor.delete()
            }
        }
    }

    override suspend fun createStats(
        transaction: Transaction,
        type: String,
        count: Long,
        firstItemListEntryId: String,
        lastItemListEntryId: String
    ) {
        transaction.statsStore { store ->
            store.add(jso<Item_list_stats> {
                item_list_type = type
                this.count = count.toDouble()
                first_item_list_id = firstItemListEntryId
                last_item_list_id = lastItemListEntryId
            })
        }
    }

    override suspend fun getStats(transaction: Transaction, type: String): ItemListStats? {
        return transaction.statsStore { store ->
            store.get(Key(type))?.unsafeCast<Item_list_stats>()?.toDomain()
        }
    }

    override suspend fun getStatsCount(transaction: Transaction, type: String): Long {
        return transaction.statsStore { store ->
            store.get(Key(type))?.unsafeCast<Item_list_stats>()?.count?.toLong() ?: 0L
        }
    }

    override suspend fun incrementStatsCount(transaction: Transaction, type: String, count: Long) {
        transaction.statsStore { store ->
            store.get(Key(type))?.unsafeCast<Item_list_stats>()?.let { stats ->
                stats.count += count.toDouble()
                store.put(stats)
            }
        }
    }

    override suspend fun decrementStatsCount(transaction: Transaction, type: String, count: Long) {
        transaction.statsStore { store ->
            store.get(Key(type))?.unsafeCast<Item_list_stats>()?.let { stats ->
                stats.count -= count.toDouble()
                store.put(stats)
            }
        }
    }

    override suspend fun updateStatsCount(transaction: Transaction, type: String, count: Long) {
        transaction.statsStore { store ->
            store.get(Key(type))?.unsafeCast<Item_list_stats>()?.let { stats ->
                stats.count = count.toDouble()
                store.put(stats)
            }
        }
    }

    override suspend fun updateStatsFirstItem(transaction: Transaction, type: String, id: String) {
        transaction.statsStore { store ->
            store.get(Key(type))?.unsafeCast<Item_list_stats>()?.let { stats ->
                stats.first_item_list_id = id
                store.put(stats)
            }
        }
    }

    override suspend fun updateStatsLastItem(transaction: Transaction, type: String, id: String) {
        transaction.statsStore { store ->
            store.get(Key(type))?.unsafeCast<Item_list_stats>()?.let { stats ->
                stats.last_item_list_id = id
                store.put(stats)
            }
        }
    }

    override suspend fun deleteStats(transaction: Transaction, type: String) {
        transaction.statsStore { store ->
            store.delete(Key(type))
        }
    }

    private inline fun <R> Transaction.store(block: WriteTransaction.(store: ObjectStore) -> R): R {
        return with(transaction) { block(transaction.objectStore("item_list")) }
    }

    private inline fun <R> Transaction.statsStore(block: WriteTransaction.(store: ObjectStore) -> R): R {
        return with(transaction) { block(transaction.objectStore("item_list_stats")) }
    }
}
