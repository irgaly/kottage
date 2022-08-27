package io.github.irgaly.kottage.strategy

interface KottageStrategy {
    fun initialize(
        operator: KottageStrategyOperator
    )

    /**
     * called in transaction
     */
    fun onItemRead(key: String, now: Long)

    /**
     * called in transaction
     */
    fun onPostItemCreate(key: String, itemCount: Long, now: Long)
}
