package net.irgaly.kkvs.strategy

interface KkvsStrategyOperator {
    suspend fun updateItemLastRead(key: String, now: Long)
    suspend fun deleteLeastRecentlyUsed(limit: Long)
    suspend fun deleteOlderItems(limit: Long)
}
