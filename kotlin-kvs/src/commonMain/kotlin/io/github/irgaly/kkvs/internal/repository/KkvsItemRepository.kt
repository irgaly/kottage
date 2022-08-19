package io.github.irgaly.kkvs.internal.repository

import io.github.irgaly.kkvs.internal.model.Item

internal interface KkvsItemRepository {
    suspend fun upsert(item: Item)
    suspend fun updateLastRead(key: String, lastReadAt: Long)
    suspend fun updateExpireAt(key: String, expireAt: Long)
    suspend fun exists(key: String): Boolean
    suspend fun get(key: String): Item?
    suspend fun getCount(): Long
    suspend fun getAllKeys(receiver: suspend (key: String) -> Unit)
    suspend fun delete(key: String)
    suspend fun deleteLeastRecentlyUsed(limit: Long)
    suspend fun deleteOlderItems(limit: Long)
    suspend fun deleteAll()
    suspend fun getStatsCount(): Long
    suspend fun incrementStatsCount(count: Long)
    suspend fun decrementStatsCount(count: Long)
    suspend fun updateStatsCount(count: Long)
    suspend fun deleteStats()
}
