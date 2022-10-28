package io.github.irgaly.kottage.strategy

/**
 * LRU Cache Strategy
 *
 * @param maxEntryCount decrease item if the item count exceeded this value
 * @param reduceCount the target count to remove, default 25% of maxEntryCount
 */
class KottageLruStrategy(
    private val maxEntryCount: Long,
    private val reduceCount: Long? = null
) : KottageStrategy {
    private val calculatedReduceCount: Long =
        (maxEntryCount * 0.25).toLong().coerceAtLeast(1)

    override fun onItemRead(
        transaction: KottageTransaction,
        key: String,
        itemType: String,
        now: Long,
        operator: KottageStrategyOperator
    ) {
        operator.updateItemLastRead(transaction, key, itemType, now)
    }

    override fun onPostItemCreate(
        transaction: KottageTransaction,
        key: String,
        itemType: String,
        itemCount: Long,
        now: Long,
        operator: KottageStrategyOperator
    ) {
        if (maxEntryCount < itemCount) {
            // expire caches
            val expiredItemsCount = operator.deleteExpiredItems(transaction, itemType, now)
            // reduce caches
            val reduceCount = (reduceCount ?: calculatedReduceCount) - expiredItemsCount
            if (0 < reduceCount) {
                operator.deleteLeastRecentlyUsed(transaction, itemType, reduceCount)
            }
        }
    }
}
