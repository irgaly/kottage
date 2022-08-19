package io.github.irgaly.kkvs.strategy

interface KkvsStrategy {
    fun initialize(
        operator: KkvsStrategyOperator
    )

    suspend fun onItemRead(key: String, now: Long)
    suspend fun onPostItemCreate(key: String, itemCount: Long, now: Long)
}
