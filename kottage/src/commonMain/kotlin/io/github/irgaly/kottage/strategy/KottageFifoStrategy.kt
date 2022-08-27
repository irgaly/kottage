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
    private lateinit var operator: KottageStrategyOperator
    private val calculatedReduceCount: Long =
        (maxEntryCount * 0.25).toLong().coerceAtLeast(1)

    override fun initialize(operator: KottageStrategyOperator) {
        this.operator = operator
    }

    override fun onItemRead(key: String, now: Long) {
        // do nothing
    }

    override fun onPostItemCreate(key: String, itemCount: Long, now: Long) {
        if (maxEntryCount < itemCount) {
            // reduce caches
            operator.deleteOlderItems(calculatedReduceCount)
        }
    }
}