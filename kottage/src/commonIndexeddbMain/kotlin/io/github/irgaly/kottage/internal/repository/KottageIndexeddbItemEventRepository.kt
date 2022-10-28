package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.model.ItemEvent

internal class KottageIndexeddbItemEventRepository : KottageItemEventRepository {
    override fun create(transaction: Transaction, itemEvent: ItemEvent) {
        TODO("Not yet implemented")
    }

    override fun selectAfter(
        transaction: Transaction,
        createdAt: Long,
        itemType: String?,
        limit: Long?
    ): List<ItemEvent> {
        TODO("Not yet implemented")
    }

    override fun getLatestCreatedAt(transaction: Transaction, itemType: String): Long? {
        TODO("Not yet implemented")
    }

    override fun getExpiredIds(
        transaction: Transaction,
        now: Long,
        itemType: String?,
        receiver: (id: String, itemType: String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getCount(transaction: Transaction, itemType: String): Long {
        TODO("Not yet implemented")
    }

    override fun delete(transaction: Transaction, id: String) {
        TODO("Not yet implemented")
    }

    override fun deleteOlderEvents(transaction: Transaction, itemType: String, limit: Long) {
        TODO("Not yet implemented")
    }

    override fun deleteBefore(transaction: Transaction, createdAt: Long) {
        TODO("Not yet implemented")
    }

    override fun deleteAll(transaction: Transaction, itemType: String) {
        TODO("Not yet implemented")
    }

    override fun deleteAllList(transaction: Transaction, listType: String) {
        TODO("Not yet implemented")
    }

    override fun getStatsCount(transaction: Transaction, itemType: String): Long {
        TODO("Not yet implemented")
    }

    override fun incrementStatsCount(transaction: Transaction, itemType: String, count: Long) {
        TODO("Not yet implemented")
    }

    override fun decrementStatsCount(transaction: Transaction, itemType: String, count: Long) {
        TODO("Not yet implemented")
    }

    override fun updateStatsCount(transaction: Transaction, itemType: String, count: Long) {
        TODO("Not yet implemented")
    }
}
