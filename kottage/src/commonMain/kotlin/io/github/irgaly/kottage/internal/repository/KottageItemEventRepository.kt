package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.model.ItemEvent

internal interface KottageItemEventRepository {
    suspend fun create(transaction: Transaction, itemEvent: ItemEvent)
    suspend fun selectAfter(
        transaction: Transaction,
        createdAt: Long,
        itemType: String? = null,
        limit: Long? = null
    ): List<ItemEvent>

    suspend fun getLatestCreatedAt(transaction: Transaction, itemType: String): Long?
    suspend fun getCount(transaction: Transaction, itemType: String): Long
    suspend fun delete(transaction: Transaction, id: String)
    suspend fun deleteExpiredEvents(
        transaction: Transaction,
        now: Long,
        itemType: String? = null,
        onDelete: (suspend (id: String, itemType: String) -> Unit)? = null
    ): Long

    suspend fun deleteOlderEvents(transaction: Transaction, itemType: String, limit: Long)
    suspend fun deleteBefore(transaction: Transaction, createdAt: Long)
    suspend fun deleteAll(transaction: Transaction, itemType: String)
    suspend fun deleteAllList(transaction: Transaction, listType: String)
    suspend fun getStatsCount(transaction: Transaction, itemType: String): Long
    suspend fun incrementStatsCount(transaction: Transaction, itemType: String, count: Long)
    suspend fun decrementStatsCount(transaction: Transaction, itemType: String, count: Long)
    suspend fun updateStatsCount(transaction: Transaction, itemType: String, count: Long)
}
