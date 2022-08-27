package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.model.Item
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class KkvsIndexeddbItemRepository(
    private val itemType: String,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : KkvsItemRepository {
    override fun upsert(item: Item) {
        TODO("Not yet implemented")
    }

    override fun updateLastRead(key: String, lastReadAt: Long) {
        TODO("Not yet implemented")
    }

    override fun updateExpireAt(key: String, expireAt: Long) {
        TODO("Not yet implemented")
    }

    override fun exists(key: String): Boolean = withContext(Dispatchers.Default) {
        TODO("Not yet implemented")
    }

    override fun get(key: String): Item? {
        TODO("Not yet implemented")
    }

    override fun getCount(): Long {
        TODO("Not yet implemented")
    }

    override fun getAllKeys(receiver: suspend (key: String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun delete(key: String) = withContext(Dispatchers.Default) {
        TODO("Not yet implemented")
    }

    override fun deleteLeastRecentlyUsed(limit: Long) {
        TODO("Not yet implemented")
    }

    override fun deleteOlderItems(limit: Long) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }

    override fun getStatsCount(): Long {
        TODO("Not yet implemented")
    }

    override fun incrementStatsCount(count: Long) {
        TODO("Not yet implemented")
    }

    override fun decrementStatsCount(count: Long) {
        TODO("Not yet implemented")
    }

    override fun updateStatsCount(count: Long) {
        TODO("Not yet implemented")
    }

    override fun deleteStats() {
        TODO("Not yet implemented")
    }
}
