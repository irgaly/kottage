package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.model.Item
import io.github.irgaly.kottage.internal.model.ItemStats

internal interface KottageItemRepository {
    suspend fun upsert(transaction: Transaction, item: Item)
    suspend fun updateLastRead(transaction: Transaction, key: String, itemType: String, lastReadAt: Long)
    suspend fun updateExpireAt(transaction: Transaction, key: String, itemType: String, expireAt: Long)
    suspend fun exists(transaction: Transaction, key: String, itemType: String): Boolean
    suspend fun get(transaction: Transaction, key: String, itemType: String): Item?
    suspend fun getCount(transaction: Transaction, itemType: String): Long
    suspend fun getAllKeys(
        transaction: Transaction,
        itemType: String,
        receiver: suspend (key: String) -> Unit
    )

    suspend fun getExpiredKeys(
        transaction: Transaction,
        now: Long,
        itemType: String? = null,
        receiver: suspend (key: String, itemType: String) -> Unit
    )

    suspend fun getLeastRecentlyUsedKeys(
        transaction: Transaction,
        itemType: String,
        limit: Long?,
        receiver: suspend (key: String) -> Boolean
    )

    suspend fun getOlderKeys(
        transaction: Transaction,
        itemType: String,
        limit: Long?,
        receiver: suspend (key: String) -> Boolean
    )

    suspend fun getStats(transaction: Transaction, itemType: String): ItemStats?
    suspend fun getEmptyStats(transaction: Transaction, limit: Long): List<ItemStats>

    suspend fun delete(transaction: Transaction, key: String, itemType: String)
    suspend fun deleteAll(transaction: Transaction, itemType: String)
    suspend fun getStatsCount(transaction: Transaction, itemType: String): Long
    suspend fun incrementStatsCount(transaction: Transaction, itemType: String, count: Long)
    suspend fun decrementStatsCount(transaction: Transaction, itemType: String, count: Long)
    suspend fun updateStatsCount(transaction: Transaction, itemType: String, count: Long)
    suspend fun getStatsByteSize(transaction: Transaction, itemType: String): Long
    suspend fun updateStatsByteSize(transaction: Transaction, itemType: String, bytes: Long)
    suspend fun deleteStats(transaction: Transaction, itemType: String)
}
