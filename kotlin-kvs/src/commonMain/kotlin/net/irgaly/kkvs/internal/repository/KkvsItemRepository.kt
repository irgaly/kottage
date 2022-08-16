package net.irgaly.kkvs.internal.repository

import net.irgaly.kkvs.internal.model.Item

internal interface KkvsItemRepository {
    suspend fun upsert(item: Item)
    suspend fun updateLastRead(key: String, lastReadAt: Long)
    suspend fun updateExpireAt(key: String, expireAt: Long)
    suspend fun exists(key: String): Boolean
    suspend fun get(key: String): Item?
    suspend fun getAllKeys(receiver: suspend (key: String) -> Unit)
    suspend fun delete(key: String)
    suspend fun deleteAll()
}
