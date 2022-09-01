package io.github.irgaly.kottage.strategy

interface KottageStrategy {
    /**
     * called in transaction
     */
    fun onItemRead(key: String, itemType: String, now: Long, operator: KottageStrategyOperator)

    /**
     * called in transaction
     */
    fun onPostItemCreate(
        key: String,
        itemType: String,
        itemCount: Long,
        now: Long,
        operator: KottageStrategyOperator
    )
}
