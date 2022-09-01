package io.github.irgaly.kottage.strategy

interface KottageStrategyOperator {
    /**
     * This should be called in transaction
     */
    fun updateItemLastRead(key: String, itemType: String, now: Long)

    /**
     * This should be called in transaction
     */
    fun deleteLeastRecentlyUsed(itemType: String, limit: Long)

    /**
     * This should be called in transaction
     */
    fun deleteOlderItems(itemType: String, limit: Long)

    /**
     * delete expired items
     * This should be called in transaction
     *
     * @return deleted items count
     */
    fun deleteExpiredItems(itemType: String, now: Long): Long
}
