package net.irgaly.kkvs.strategy

class KkvsFifoStrategy: KkvsStrategy {
    override fun initialize(operator: KkvsStrategyOperator) {
        TODO("Not yet implemented")
    }

    override suspend fun onItemRead(key: String, now: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun onItemCreate(key: String, itemCount: Long, now: Long) {
        TODO("Not yet implemented")
    }
}
