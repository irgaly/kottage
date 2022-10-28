package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.model.ItemEvent

internal class KottageIndexeddbItemEventRepository : KottageItemEventRepository {
    override suspend fun create(transaction: Transaction, itemEvent: ItemEvent) {
        TODO("Not yet implemented")
    }

    override suspend fun selectAfter(
        transaction: Transaction,
        createdAt: Long,
        itemType: String?,
        limit: Long?
    ): List<ItemEvent> {
        TODO("Not yet implemented")
    }

    override suspend fun getLatestCreatedAt(transaction: Transaction, itemType: String): Long? {
        TODO("Not yet implemented")
    }

    override suspend fun getExpiredIds(
        transaction: Transaction,
        now: Long,
        itemType: String?,
        receiver: suspend (id: String, itemType: String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getCount(transaction: Transaction, itemType: String): Long {
        TODO("Not yet implemented")
    }

    override suspend fun delete(transaction: Transaction, id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteOlderEvents(transaction: Transaction, itemType: String, limit: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteBefore(transaction: Transaction, createdAt: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll(transaction: Transaction, itemType: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllList(transaction: Transaction, listType: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getStatsCount(transaction: Transaction, itemType: String): Long {
        TODO("Not yet implemented")
    }

    override suspend fun incrementStatsCount(transaction: Transaction, itemType: String, count: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun decrementStatsCount(transaction: Transaction, itemType: String, count: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun updateStatsCount(transaction: Transaction, itemType: String, count: Long) {
        TODO("Not yet implemented")
    }
}
