package io.github.irgaly.kottage.strategy

interface KottageStrategyOperator {
    /**
     * This should be called in transaction
     */
    suspend fun updateItemLastRead(transaction: KottageTransaction, key: String, itemType: String, now: Long)

    /**
     * delete least recently used items
     * existing items in ItemList are ignored
     * This should be called in transaction
     *
     * @return (deleted items count, deleted items size)
     */
    suspend fun deleteLeastRecentlyUsed(
        transaction: KottageTransaction,
        itemType: String,
        limit: Long
    ): Pair<Long, Long>

    /**
     * delete least recently used items at least bytes
     * existing items in ItemList are ignored
     * This should be called in transaction
     *
     * @return (deleted items count, deleted items size)
     */
    suspend fun deleteLeastRecentlyUsedByBytes(
        transaction: KottageTransaction,
        itemType: String,
        atLeastBytes: Long
    ): Pair<Long, Long>

    /**
     * delete older created items
     * existing items in ItemList are ignored
     * This should be called in transaction
     *
     * @return (deleted items count, deleted items size)
     */
    suspend fun deleteOlderItems(
        transaction: KottageTransaction,
        itemType: String,
        limit: Long
    ): Pair<Long, Long>

    /**
     * delete older created items at least bytes
     * existing items in ItemList are ignored
     * This should be called in transaction
     *
     * @return (deleted items count, deleted items size)
     */
    suspend fun deleteOlderItemsByBytes(
        transaction: KottageTransaction,
        itemType: String,
        atLeastBytes: Long
    ): Pair<Long, Long>

    /**
     * delete expired items
     * existing items in ItemList are ignored
     * This should be called in transaction
     *
     * @return (deleted items count, deleted items size)
     */
    suspend fun deleteExpiredItems(
        transaction: KottageTransaction,
        itemType: String,
        now: Long
    ): Pair<Long, Long>
}
