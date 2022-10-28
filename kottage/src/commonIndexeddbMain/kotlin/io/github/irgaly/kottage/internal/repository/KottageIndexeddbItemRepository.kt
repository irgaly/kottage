package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.model.Item
import io.github.irgaly.kottage.internal.model.ItemStats

internal class KottageIndexeddbItemRepository : KottageItemRepository {
    override suspend fun upsert(transaction: Transaction, item: Item) {
        TODO("Not yet implemented")
    }

    override suspend fun updateLastRead(
        transaction: Transaction,
        key: String,
        itemType: String,
        lastReadAt: Long
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun updateExpireAt(
        transaction: Transaction,
        key: String,
        itemType: String,
        expireAt: Long
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun exists(transaction: Transaction, key: String, itemType: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun get(transaction: Transaction, key: String, itemType: String): Item? {
        TODO("Not yet implemented")
    }

    override suspend fun getCount(transaction: Transaction, itemType: String): Long {
        TODO("Not yet implemented")
    }

    override suspend fun getAllKeys(
        transaction: Transaction,
        itemType: String,
        receiver: suspend (key: String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getExpiredKeys(
        transaction: Transaction,
        now: Long,
        itemType: String?,
        receiver: suspend (key: String, itemType: String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getLeastRecentlyUsedKeys(
        transaction: Transaction,
        itemType: String,
        limit: Long?,
        receiver: suspend (key: String) -> Boolean
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getOlderKeys(
        transaction: Transaction,
        itemType: String,
        limit: Long?,
        receiver: suspend (key: String) -> Boolean
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getStats(transaction: Transaction, itemType: String): ItemStats? {
        TODO("Not yet implemented")
    }

    override suspend fun getEmptyStats(transaction: Transaction, limit: Long): List<ItemStats> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(transaction: Transaction, key: String, itemType: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll(transaction: Transaction, itemType: String) {
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

    override suspend fun deleteStats(transaction: Transaction, itemType: String) {
        TODO("Not yet implemented")
    }
}
