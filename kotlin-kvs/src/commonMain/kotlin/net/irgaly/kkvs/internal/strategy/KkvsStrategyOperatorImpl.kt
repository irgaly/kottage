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
