package net.irgaly.kkvs.internal.strategy

import net.irgaly.kkvs.internal.KkvsDatabaseManager
import net.irgaly.kkvs.strategy.KkvsStrategyOperator

internal class KkvsStrategyOperatorImpl(
    private val databaseManager: KkvsDatabaseManager,
    val itemType: String
): KkvsStrategyOperator {
    private val itemRepository by lazy {
        databaseManager.getItemRepository(itemType)
    }

    override suspend fun updateItemLastRead(key: String, now: Long) {
        itemRepository.updateLastRead(key, now)
    }
}
