package io.github.irgaly.kottage.strategy

/**
 * LRU Cache Strategy
 *
 * The maxEntryCount or maxCacheSize must be specified.
 * Both of count based configuration and size based configuration can be enabled at the same time.
 *
 * @param maxEntryCount decrease item if the item count exceeded this value
 * @param reduceCount the target count to remove, default 25% of maxEntryCount
 * @param maxCacheSize the size of bytes that decrease item if the estimate total items size exceeded it
 * @param reduceSize the target size to remove, default 25% of maxCacheSize
 */
class KottageLruStrategy(
    private val maxEntryCount: Long? = null,
    private val reduceCount: Long? = null,
    private val maxCacheSize: Long? = null,
    private val reduceSize: Long? = null,
) : KottageStrategy {
    private val calculatedReduceCount: Long =
        ((maxEntryCount ?: 0) * 0.25).toLong().coerceAtLeast(1)
    private val calculatedReduceSize: Long =
        ((maxCacheSize ?: 0) * 0.25).toLong().coerceAtLeast(1)

    init {
        if (maxEntryCount == null && maxCacheSize == null) {
            error("maxEntryCount or maxCacheSize must be specified")
        }
    }

    override suspend fun onItemRead(
        transaction: KottageTransaction,
        key: String,
        itemType: String,
        now: Long,
        operator: KottageStrategyOperator
    ) {
        operator.updateItemLastRead(transaction, key, itemType, now)
    }

    override suspend fun onPostItemCreate(
        transaction: KottageTransaction,
        key: String,
        itemType: String,
        itemCount: Long,
        estimateTotalBytes: Long,
        now: Long,
        operator: KottageStrategyOperator
    ) {
        val reduceByCount = (maxEntryCount != null && maxEntryCount < itemCount)
        val reduceBySize = (maxCacheSize != null && maxCacheSize < estimateTotalBytes)
        var deletedItemsCount: Long = 0
        var deletedItemsSize: Long = 0
        if (reduceByCount || reduceBySize) {
            // expire caches
            val result = operator.deleteExpiredItems(transaction, itemType, now)
            deletedItemsCount += result.first
            deletedItemsSize += result.second
        }
        if (reduceByCount) {
            // reduce caches in count
            val reduceCount = (reduceCount ?: calculatedReduceCount) - deletedItemsCount
            if (0 < reduceCount) {
                val result = operator.deleteLeastRecentlyUsed(transaction, itemType, reduceCount)
                deletedItemsSize += result.second
            }
        }
        if (reduceBySize) {
            // reduce caches in size
            val reduceSize = (reduceSize ?: calculatedReduceSize) - deletedItemsSize
            if (0 < reduceSize) {
                operator.deleteLeastRecentlyUsedByBytes(transaction, itemType, reduceSize)
            }
        }
    }
}
