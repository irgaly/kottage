package io.github.irgaly.kottage.strategy

/**
 * FIFO Strategy
 *
 * @param maxEntryCount decrease item if the item count exceeded this value
 * @param reduceCount the target count to remove, default 25% of maxEntryCount
 */
class KottageFifoStrategy(
    private val maxEntryCount: Long,
    private val reduceCount: Long? = null
) : KottageStrategy {
    private val calculatedReduceCount: Long =
        (maxEntryCount * 0.25).toLong().coerceAtLeast(1)

    override fun onItemRead(
        key: String,
        itemType: String,
        now: Long,
        operator: KottageStrategyOperator
    ) {
        // do nothing
    }

    override fun onPostItemCreate(
        key: String,
        itemType: String,
        itemCount: Long,
        now: Long,
        operator: KottageStrategyOperator
    ) {
        if (maxEntryCount < itemCount) {
            // expire caches
            val expiredItemsCount = operator.deleteExpiredItems(itemType, now)
            // reduce caches
            val reduceCount = calculatedReduceCount - expiredItemsCount
            if (0 < reduceCount) {
                operator.deleteOlderItems(itemType, reduceCount)
            }
        }
    }
}
