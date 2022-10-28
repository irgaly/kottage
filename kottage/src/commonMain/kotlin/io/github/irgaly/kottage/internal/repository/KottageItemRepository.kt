package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.model.Item
import io.github.irgaly.kottage.internal.model.ItemStats

internal interface KottageItemRepository {
    fun upsert(transaction: Transaction, item: Item)
    fun updateLastRead(transaction: Transaction, key: String, itemType: String, lastReadAt: Long)
    fun updateExpireAt(transaction: Transaction, key: String, itemType: String, expireAt: Long)
    fun exists(transaction: Transaction, key: String, itemType: String): Boolean
    fun get(transaction: Transaction, key: String, itemType: String): Item?
    fun getCount(transaction: Transaction, itemType: String): Long
    fun getAllKeys(
        transaction: Transaction,
        itemType: String,
        receiver: (key: String) -> Unit
    )

    fun getExpiredKeys(
        transaction: Transaction,
        now: Long,
        itemType: String? = null,
        receiver: (key: String, itemType: String) -> Unit
    )

    fun getLeastRecentlyUsedKeys(
        transaction: Transaction,
        itemType: String,
        limit: Long?,
        receiver: (key: String) -> Boolean
    )

    fun getOlderKeys(
        transaction: Transaction,
        itemType: String,
        limit: Long?,
        receiver: (key: String) -> Boolean
    )

    fun getStats(transaction: Transaction, itemType: String): ItemStats?
    fun getEmptyStats(transaction: Transaction, limit: Long): List<ItemStats>

    fun delete(transaction: Transaction, key: String, itemType: String)
    fun deleteAll(transaction: Transaction, itemType: String)
    fun getStatsCount(transaction: Transaction, itemType: String): Long
    fun incrementStatsCount(transaction: Transaction, itemType: String, count: Long)
    fun decrementStatsCount(transaction: Transaction, itemType: String, count: Long)
    fun updateStatsCount(transaction: Transaction, itemType: String, count: Long)
    fun deleteStats(transaction: Transaction, itemType: String)
}
