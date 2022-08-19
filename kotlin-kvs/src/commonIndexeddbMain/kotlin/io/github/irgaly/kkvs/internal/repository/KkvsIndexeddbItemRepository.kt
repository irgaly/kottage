package io.github.irgaly.kkvs.internal.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.github.irgaly.kkvs.internal.model.Item

internal class KkvsIndexeddbItemRepository(private val itemType: String) : KkvsItemRepository {
    override suspend fun upsert(item: Item) {
        TODO("Not yet implemented")
    }

    override suspend fun updateLastRead(key: String, lastReadAt: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun updateExpireAt(key: String, expireAt: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun exists(key: String): Boolean = withContext(Dispatchers.Default) {
        TODO("Not yet implemented")
    }

    override suspend fun get(key: String): Item? {
        TODO("Not yet implemented")
    }

    override suspend fun getCount(): Long {
        TODO("Not yet implemented")
    }

    override suspend fun getAllKeys(receiver: suspend (key: String) -> Unit) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(key: String) = withContext(Dispatchers.Default) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteLeastRecentlyUsed(limit: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteOlderItems(limit: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll() {
        TODO("Not yet implemented")
    }

    override suspend fun getStatsCount(): Long {
        TODO("Not yet implemented")
    }

    override suspend fun incrementStatsCount(count: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun decrementStatsCount(count: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun updateStatsCount(count: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteStats() {
        TODO("Not yet implemented")
    }
}