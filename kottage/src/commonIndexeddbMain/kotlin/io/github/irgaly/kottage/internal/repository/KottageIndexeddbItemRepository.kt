package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.model.Item
import io.github.irgaly.kottage.internal.model.ItemStats

internal class KottageIndexeddbItemRepository : KottageItemRepository {
    override fun upsert(item: Item) {
        TODO("Not yet implemented")
    }

    override fun updateLastRead(key: String, itemType: String, lastReadAt: Long) {
        TODO("Not yet implemented")
    }

    override fun updateExpireAt(key: String, itemType: String, expireAt: Long) {
        TODO("Not yet implemented")
    }

    override fun exists(key: String, itemType: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun get(key: String, itemType: String): Item? {
        TODO("Not yet implemented")
    }

    override fun getCount(itemType: String): Long {
        TODO("Not yet implemented")
    }

    override fun getAllKeys(itemType: String, receiver: (key: String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getExpiredKeys(
        now: Long,
        itemType: String?,
        receiver: (key: String, itemType: String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getLeastRecentlyUsedKeys(
        itemType: String,
        limit: Long?,
        receiver: (key: String) -> Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun getOlderKeys(itemType: String, limit: Long?, receiver: (key: String) -> Boolean) {
        TODO("Not yet implemented")
    }

    override fun getStats(itemType: String): ItemStats? {
        TODO("Not yet implemented")
    }

    override fun getEmptyStats(limit: Long): List<ItemStats> {
        TODO("Not yet implemented")
    }

    override fun delete(key: String, itemType: String) {
        TODO("Not yet implemented")
    }

    override fun deleteAll(itemType: String) {
        TODO("Not yet implemented")
    }

    override fun getStatsCount(itemType: String): Long {
        TODO("Not yet implemented")
    }

    override fun incrementStatsCount(itemType: String, count: Long) {
        TODO("Not yet implemented")
    }

    override fun decrementStatsCount(itemType: String, count: Long) {
        TODO("Not yet implemented")
    }

    override fun updateStatsCount(itemType: String, count: Long) {
        TODO("Not yet implemented")
    }

    override fun deleteStats(itemType: String) {
        TODO("Not yet implemented")
    }
}
