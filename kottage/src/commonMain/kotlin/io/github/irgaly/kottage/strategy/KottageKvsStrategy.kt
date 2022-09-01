package io.github.irgaly.kottage.strategy

/**
 * No Eviction Strategy
 */
class KottageKvsStrategy : KottageStrategy {
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
        // do nothing
    }
}
