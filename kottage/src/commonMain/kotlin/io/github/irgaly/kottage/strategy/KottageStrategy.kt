package io.github.irgaly.kottage.strategy

interface KottageStrategy {
    /**
     * called in transaction
     */
    fun onItemRead(
        transaction: KottageTransaction,
        key: String,
        itemType: String,
        now: Long, operator: KottageStrategyOperator
    )

    /**
     * called in transaction
     */
    fun onPostItemCreate(
        transaction: KottageTransaction,
        key: String,
        itemType: String,
        itemCount: Long,
        now: Long,
        operator: KottageStrategyOperator
    )
}
