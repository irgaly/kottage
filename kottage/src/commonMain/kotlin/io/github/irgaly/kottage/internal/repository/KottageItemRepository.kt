package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.model.Item

internal interface KottageItemRepository {
    fun upsert(item: Item)
    fun updateLastRead(key: String, itemType: String, lastReadAt: Long)
    fun updateExpireAt(key: String, itemType: String, expireAt: Long)
    fun exists(key: String, itemType: String): Boolean
    fun get(key: String, itemType: String): Item?
    fun getCount(itemType: String): Long
    fun getAllKeys(itemType: String, receiver: (key: String) -> Unit)
    fun getExpiredKeys(
        now: Long,
        itemType: String? = null,
        receiver: (key: String, itemType: String) -> Unit
    )

    fun getLeastRecentlyUsedKeys(
        itemType: String,
        limit: Long,
        receiver: (key: String, itemType: String) -> Unit
    )

    fun getOlderKeys(
        itemType: String,
        limit: Long,
        receiver: (key: String, itemType: String) -> Unit
    )

    fun delete(key: String, itemType: String)
    fun deleteAll(itemType: String)
    fun getStatsCount(itemType: String): Long
    fun incrementStatsCount(itemType: String, count: Long)
    fun decrementStatsCount(itemType: String, count: Long)
    fun updateStatsCount(itemType: String, count: Long)
    fun deleteStats(itemType: String)
}
