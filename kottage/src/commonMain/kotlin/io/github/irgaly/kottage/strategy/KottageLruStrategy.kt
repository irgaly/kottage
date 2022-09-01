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
    private lateinit var operator: KottageStrategyOperator
    private val calculatedReduceCount: Long =
        (maxEntryCount * 0.25).toLong().coerceAtLeast(1)

    override fun initialize(operator: KottageStrategyOperator) {
        this.operator = operator
    }

    override fun onItemRead(key: String, itemType: String, now: Long) {
        operator.updateItemLastRead(key, itemType, now)
    }

    override fun onPostItemCreate(key: String, itemType: String, itemCount: Long, now: Long) {
        if (maxEntryCount < itemCount) {
            // expire caches
            val expiredItemsCount = operator.deleteExpiredItems(itemType, now)
            // reduce caches
            val reduceCount = calculatedReduceCount - expiredItemsCount
            if (0 < reduceCount) {
                operator.deleteLeastRecentlyUsed(itemType, reduceCount)
            }
        }
    }
}
