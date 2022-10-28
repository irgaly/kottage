package io.github.irgaly.kottage.strategy

interface KottageStrategyOperator {
    /**
     * This should be called in transaction
     */
    fun updateItemLastRead(transaction: KottageTransaction, key: String, itemType: String, now: Long)

    /**
     * delete least recently used items
     * existing items in ItemList are ignored
     * This should be called in transaction
     */
    fun deleteLeastRecentlyUsed(transaction: KottageTransaction, itemType: String, limit: Long)

    /**
     * delete older created items
     * existing items in ItemList are ignored
     * This should be called in transaction
     */
    fun deleteOlderItems(transaction: KottageTransaction, itemType: String, limit: Long)

    /**
     * delete expired items
     * existing items in ItemList are ignored
     * This should be called in transaction
     *
     * @return deleted items count
     */
    fun deleteExpiredItems(transaction: KottageTransaction, itemType: String, now: Long): Long
}
