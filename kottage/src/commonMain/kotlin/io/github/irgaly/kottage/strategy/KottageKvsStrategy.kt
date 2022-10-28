package io.github.irgaly.kottage.strategy

/**
 * No Eviction Strategy
 */
class KottageKvsStrategy : KottageStrategy {
    override suspend fun onItemRead(
        transaction: KottageTransaction,
        key: String,
        itemType: String,
        now: Long,
        operator: KottageStrategyOperator
    ) {
        // do nothing
    }

    override suspend fun onPostItemCreate(
        transaction: KottageTransaction,
        key: String,
        itemType: String,
        itemCount: Long,
        now: Long,
        operator: KottageStrategyOperator
    ) {
        // do nothing
    }
}
