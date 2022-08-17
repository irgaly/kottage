package net.irgaly.kkvs.strategy

interface KkvsStrategy {
    fun initialize(
        operator: KkvsStrategyOperator
    )

    suspend fun onItemRead(key: String, now: Long)
    suspend fun onItemCreate(key: String, itemCount: Long, now: Long)
}
