package io.github.irgaly.kottage.internal.strategy

import io.github.irgaly.kottage.internal.KottageDatabaseManager
import io.github.irgaly.kottage.strategy.KottageStrategyOperator

internal class KottageStrategyOperatorImpl(
    private val databaseManager: KottageDatabaseManager,
    val itemType: String
) : KottageStrategyOperator {
    private val itemRepository by lazy {
        databaseManager.getItemRepository(itemType)
    }

    override fun updateItemLastRead(key: String, now: Long) {
        itemRepository.updateLastRead(key, now)
    }

    override fun deleteLeastRecentlyUsed(limit: Long) {
        itemRepository.deleteLeastRecentlyUsed(limit)
        val count = itemRepository.getCount()
        itemRepository.updateStatsCount(count)
    }

    override fun deleteOlderItems(limit: Long) {
        itemRepository.deleteOlderItems(limit)
        val count = itemRepository.getCount()
        itemRepository.updateStatsCount(count)
    }
}
