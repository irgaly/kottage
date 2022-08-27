package io.github.irgaly.kkvs.strategy

/**
 * No Eviction Strategy
 */
class KkvsKvsStrategy: KkvsStrategy {
    override fun initialize(operator: KkvsStrategyOperator) {
        // do nothing
    }

    override fun onItemRead(key: String, now: Long) {
        // do nothing
    }

    override fun onPostItemCreate(key: String, itemCount: Long, now: Long) {
        // do nothing
    }
}
