package io.github.irgaly.kottage.strategy

interface KottageStrategyOperator {
    /**
     * This should be called in transaction
     */
    fun updateItemLastRead(key: String, itemType: String, now: Long)

    /**
     * delete least recently used items
     * existing items in ItemList are ignored
     * This should be called in transaction
     */
    fun deleteLeastRecentlyUsed(itemType: String, limit: Long)

    /**
     * delete older created items
     * existing items in ItemList are ignored
     * This should be called in transaction
     */
    fun deleteOlderItems(itemType: String, limit: Long)

    /**
     * delete expired items
     * existing items in ItemList are ignored
     * This should be called in transaction
     *
     * @return deleted items count
     */
    fun deleteExpiredItems(itemType: String, now: Long): Long
}
