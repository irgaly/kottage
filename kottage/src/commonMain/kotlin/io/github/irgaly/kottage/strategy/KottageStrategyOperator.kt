package io.github.irgaly.kottage.strategy

interface KottageStrategyOperator {
    fun updateItemLastRead(key: String, now: Long)
    fun deleteLeastRecentlyUsed(limit: Long)
    fun deleteOlderItems(limit: Long)
}
