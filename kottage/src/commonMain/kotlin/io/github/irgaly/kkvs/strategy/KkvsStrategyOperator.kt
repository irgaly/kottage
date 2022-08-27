package io.github.irgaly.kkvs.strategy

interface KkvsStrategyOperator {
    fun updateItemLastRead(key: String, now: Long)
    fun deleteLeastRecentlyUsed(limit: Long)
    fun deleteOlderItems(limit: Long)
}
