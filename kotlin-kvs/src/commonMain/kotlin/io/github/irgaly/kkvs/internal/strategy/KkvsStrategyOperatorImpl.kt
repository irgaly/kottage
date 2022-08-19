package io.github.irgaly.kkvs.internal.strategy

import io.github.irgaly.kkvs.internal.KkvsDatabaseManager
import io.github.irgaly.kkvs.strategy.KkvsStrategyOperator

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

    override suspend fun deleteLeastRecentlyUsed(limit: Long) {
        itemRepository.deleteLeastRecentlyUsed(limit)
        val count = itemRepository.getCount()
        itemRepository.updateStatsCount(count)
    }

    override suspend fun deleteOlderItems(limit: Long) {
        itemRepository.deleteOlderItems(limit)
        val count = itemRepository.getCount()
        itemRepository.updateStatsCount(count)
    }
}
