package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.model.ItemListEntry
import io.github.irgaly.kottage.internal.model.ItemListStats

internal interface KottageItemListRepository {
    fun upsert(transaction: Transaction, entry: ItemListEntry)
    fun updatePreviousId(transaction: Transaction, id: String, previousId: String?)
    fun updateNextId(transaction: Transaction, id: String, nextId: String?)
    fun updateItemKey(
        transaction: Transaction,
        id: String,
        itemType: String,
        itemKey: String?,
        expireAt: Long?
    )

    fun updateExpireAt(transaction: Transaction, id: String, expireAt: Long?)
    fun removeItemKey(transaction: Transaction, id: String)
    fun removeUserData(transaction: Transaction, id: String)
    fun get(transaction: Transaction, id: String): ItemListEntry?
    fun getIds(transaction: Transaction, itemType: String, itemKey: String): List<String>
    fun getInvalidatedItemIds(
        transaction: Transaction,
        type: String,
        beforeExpireAt: Long?,
        limit: Long
    ): List<String>

    fun getCount(transaction: Transaction, type: String): Long
    fun getInvalidatedItemCount(transaction: Transaction, type: String): Long
    fun getAllTypes(
        transaction: Transaction,
        receiver: (type: String) -> Unit
    )

    fun delete(transaction: Transaction, id: String)
    fun deleteAll(transaction: Transaction, type: String)
    fun createStats(
        transaction: Transaction,
        type: String,
        count: Long,
        firstItemListEntryId: String,
        lastItemListEntryId: String
    )

    fun getStats(transaction: Transaction, type: String): ItemListStats?
    fun getStatsCount(transaction: Transaction, type: String): Long
    fun incrementStatsCount(transaction: Transaction, type: String, count: Long)
    fun decrementStatsCount(transaction: Transaction, type: String, count: Long)
    fun updateStatsCount(transaction: Transaction, type: String, count: Long)
    fun updateStatsFirstItem(transaction: Transaction, type: String, id: String)
    fun updateStatsLastItem(transaction: Transaction, type: String, id: String)
    fun deleteStats(transaction: Transaction, type: String)
}
