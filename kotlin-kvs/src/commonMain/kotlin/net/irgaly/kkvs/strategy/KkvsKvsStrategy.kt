package net.irgaly.kkvs.strategy

/**
 * No Eviction Strategy
 */
class KkvsKvsStrategy: KkvsStrategy {
    override fun initialize(operator: KkvsStrategyOperator) {
        // do nothing
    }

    override suspend fun onItemRead(key: String, now: Long) {
        // do nothing
    }

    override suspend fun onPostItemCreate(key: String, itemCount: Long, now: Long) {
        // do nothing
    }
}
