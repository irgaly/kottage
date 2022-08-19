package io.github.irgaly.kkvs.strategy

/**
 * LRU Cache Strategy
 *
 * @param maxEntryCount decrease item if the item count exceeded this value
 * @param reduceCount the target count to remove, default 25% of maxEntryCount
 */
class KkvsLruStrategy(
    private val maxEntryCount: Long,
    private val reduceCount: Long? = null
) : KkvsStrategy {
    private lateinit var operator: KkvsStrategyOperator
    private val calculatedReduceCount: Long =
        (maxEntryCount * 0.25).toLong().coerceAtLeast(1)

    override fun initialize(operator: KkvsStrategyOperator) {
        this.operator = operator
    }

    override suspend fun onItemRead(key: String, now: Long) {
        operator.updateItemLastRead(key, now)
    }

    override suspend fun onPostItemCreate(key: String, itemCount: Long, now: Long) {
        if (maxEntryCount < itemCount) {
            // reduce caches
            operator.deleteLeastRecentlyUsed(calculatedReduceCount)
        }
    }
}