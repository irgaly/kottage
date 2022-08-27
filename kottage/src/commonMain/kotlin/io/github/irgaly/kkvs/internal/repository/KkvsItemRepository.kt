package io.github.irgaly.kkvs.internal.repository

import io.github.irgaly.kkvs.internal.model.Item

internal interface KkvsItemRepository {
    fun upsert(item: Item)
    fun updateLastRead(key: String, lastReadAt: Long)
    fun updateExpireAt(key: String, expireAt: Long)
    fun exists(key: String): Boolean
    fun get(key: String): Item?
    fun getCount(): Long
    fun getAllKeys(receiver: (key: String) -> Unit)
    fun delete(key: String)
    fun deleteLeastRecentlyUsed(limit: Long)
    fun deleteOlderItems(limit: Long)
    fun deleteAll()
    fun getStatsCount(): Long
    fun incrementStatsCount(count: Long)
    fun decrementStatsCount(count: Long)
    fun updateStatsCount(count: Long)
    fun deleteStats()
}
