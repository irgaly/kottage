package io.github.irgaly.kkvs.strategy

interface KkvsStrategy {
    fun initialize(
        operator: KkvsStrategyOperator
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
