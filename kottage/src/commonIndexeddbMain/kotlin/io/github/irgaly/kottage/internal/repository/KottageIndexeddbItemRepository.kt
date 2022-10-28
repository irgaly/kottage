package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.model.Item
import io.github.irgaly.kottage.internal.model.ItemStats

internal class KottageIndexeddbItemRepository : KottageItemRepository {
    override fun upsert(transaction: Transaction, item: Item) {
        TODO("Not yet implemented")
    }

    override fun updateLastRead(
        transaction: Transaction,
        key: String,
        itemType: String,
        lastReadAt: Long
    ) {
        TODO("Not yet implemented")
    }

    override fun updateExpireAt(
        transaction: Transaction,
        key: String,
        itemType: String,
        expireAt: Long
    ) {
        TODO("Not yet implemented")
    }

    override fun exists(transaction: Transaction, key: String, itemType: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun get(transaction: Transaction, key: String, itemType: String): Item? {
        TODO("Not yet implemented")
    }

    override fun getCount(transaction: Transaction, itemType: String): Long {
        TODO("Not yet implemented")
    }

    override fun getAllKeys(
        transaction: Transaction,
        itemType: String,
        receiver: (key: String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getExpiredKeys(
        transaction: Transaction,
        now: Long,
        itemType: String?,
        receiver: (key: String, itemType: String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getLeastRecentlyUsedKeys(
        transaction: Transaction,
        itemType: String,
        limit: Long?,
        receiver: (key: String) -> Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun getOlderKeys(
        transaction: Transaction,
        itemType: String,
        limit: Long?,
        receiver: (key: String) -> Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun getStats(transaction: Transaction, itemType: String): ItemStats? {
        TODO("Not yet implemented")
    }

    override fun getEmptyStats(transaction: Transaction, limit: Long): List<ItemStats> {
        TODO("Not yet implemented")
    }

    override fun delete(transaction: Transaction, key: String, itemType: String) {
        TODO("Not yet implemented")
    }

    override fun deleteAll(transaction: Transaction, itemType: String) {
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

    override fun deleteStats(transaction: Transaction, itemType: String) {
        TODO("Not yet implemented")
    }
}
