package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.model.ItemEvent

internal interface KottageItemEventRepository {
    fun create(transaction: Transaction, itemEvent: ItemEvent)
    fun selectAfter(
        transaction: Transaction,
        createdAt: Long,
        itemType: String? = null,
        limit: Long? = null
    ): List<ItemEvent>

    fun getLatestCreatedAt(transaction: Transaction, itemType: String): Long?
    fun getExpiredIds(
        transaction: Transaction,
        now: Long,
        itemType: String? = null,
        receiver: (id: String, itemType: String) -> Unit
    )

    fun getCount(transaction: Transaction, itemType: String): Long
    fun delete(transaction: Transaction, id: String)
    fun deleteOlderEvents(transaction: Transaction, itemType: String, limit: Long)
    fun deleteBefore(transaction: Transaction, createdAt: Long)
    fun deleteAll(transaction: Transaction, itemType: String)
    fun deleteAllList(transaction: Transaction, listType: String)
    fun getStatsCount(transaction: Transaction, itemType: String): Long
    fun incrementStatsCount(transaction: Transaction, itemType: String, count: Long)
    fun decrementStatsCount(transaction: Transaction, itemType: String, count: Long)
    fun updateStatsCount(transaction: Transaction, itemType: String, count: Long)
}
