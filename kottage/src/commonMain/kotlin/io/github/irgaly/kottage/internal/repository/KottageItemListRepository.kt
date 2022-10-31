package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.model.ItemListEntry
import io.github.irgaly.kottage.internal.model.ItemListStats

internal interface KottageItemListRepository {
    suspend fun upsert(transaction: Transaction, entry: ItemListEntry)
    suspend fun updatePreviousId(transaction: Transaction, id: String, previousId: String?)
    suspend fun updateNextId(transaction: Transaction, id: String, nextId: String?)
    suspend fun updateItemKey(
        transaction: Transaction,
        id: String,
        itemType: String,
        itemKey: String?,
        expireAt: Long?
    )

    suspend fun updateExpireAt(transaction: Transaction, id: String, expireAt: Long?)
    suspend fun removeItemKey(transaction: Transaction, id: String)
    suspend fun removeUserData(transaction: Transaction, id: String)
    suspend fun get(transaction: Transaction, id: String): ItemListEntry?
    suspend fun getIds(transaction: Transaction, itemType: String, itemKey: String): List<String>
    suspend fun getInvalidatedItemIds(
        transaction: Transaction,
        type: String,
        beforeExpireAt: Long?,
        limit: Long
    ): List<String>

    suspend fun getCount(transaction: Transaction, type: String): Long
    suspend fun getInvalidatedItemCount(transaction: Transaction, type: String): Long
    suspend fun getAllTypes(
        transaction: Transaction,
        receiver: suspend (type: String) -> Unit
    )

    suspend fun delete(transaction: Transaction, id: String)
    suspend fun deleteAll(transaction: Transaction, type: String)
    suspend fun createStats(
        transaction: Transaction,
        type: String,
        count: Long,
        firstItemListEntryId: String,
        lastItemListEntryId: String
    )

    suspend fun getStats(transaction: Transaction, type: String): ItemListStats?
    suspend fun getStatsCount(transaction: Transaction, type: String): Long
    suspend fun incrementStatsCount(transaction: Transaction, type: String, count: Long)
    suspend fun decrementStatsCount(transaction: Transaction, type: String, count: Long)
    suspend fun updateStatsCount(transaction: Transaction, type: String, count: Long)
    suspend fun updateStatsFirstItem(transaction: Transaction, type: String, id: String)
    suspend fun updateStatsLastItem(transaction: Transaction, type: String, id: String)
    suspend fun deleteStats(transaction: Transaction, type: String)
}
