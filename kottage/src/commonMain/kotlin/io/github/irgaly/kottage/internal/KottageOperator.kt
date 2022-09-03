package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.internal.model.Item
import io.github.irgaly.kottage.internal.model.ItemEvent
import io.github.irgaly.kottage.internal.model.ItemEventType
import io.github.irgaly.kottage.internal.repository.KottageItemEventRepository
import io.github.irgaly.kottage.internal.repository.KottageItemRepository
import io.github.irgaly.kottage.strategy.KottageStrategyOperator

/**
 * Data Operation Logic
 */
internal class KottageOperator(
    private val itemRepository: KottageItemRepository,
    private val itemEventRepository: KottageItemEventRepository
): KottageStrategyOperator {
    fun getOrNull(key: String, itemType: String, now: Long): Item? {
        var item = itemRepository.get(key, itemType)
        if (item?.isExpired(now) == true) {
            // delete cache
            item = null
            itemRepository.delete(key, itemType)
            itemRepository.decrementStatsCount(itemType, 1)
            itemEventRepository.create(
                ItemEvent(
                    createdAt = now,
                    itemType = itemType,
                    itemKey = key,
                    eventType = ItemEventType.Expired
                )
            )
        }
        return item
    }

    /**
     * Delete expired items
     * This should be called in transaction
     */
    fun evictCache(now: Long, itemType: String? = null) {
        if (itemType != null) {
            itemRepository.getExpiredKeys(now, itemType) { key, _ ->
                deleteExpiredItem(key, itemType, now)
            }
        } else {
            itemRepository.getExpiredKeys(now) { key, itemType ->
                deleteExpiredItem(key, itemType, now)
            }
        }
    }

    override fun updateItemLastRead(key: String, itemType: String, now: Long) {
        itemRepository.updateLastRead(key, itemType, now)
    }

    override fun deleteLeastRecentlyUsed(itemType: String, limit: Long) {
        itemRepository.deleteLeastRecentlyUsed(itemType, limit)
        val count = itemRepository.getCount(itemType)
        itemRepository.updateStatsCount(itemType, count)
    }

    override fun deleteOlderItems(itemType: String, limit: Long) {
        itemRepository.deleteOlderItems(itemType, limit)
        val count = itemRepository.getCount(itemType)
        itemRepository.updateStatsCount(itemType, count)
    }

    override fun deleteExpiredItems(itemType: String, now: Long): Long {
        var deleted = 0L
        itemRepository.getExpiredKeys(now, itemType) { key, _ ->
            deleteExpiredItem(key, itemType, now)
            deleted++
        }
        return deleted
    }

    /**
     * Delete expired items
     *
     * * delete item
     * * add expired event
     */
    private fun deleteExpiredItem(key: String, itemType: String, now: Long) {
        itemRepository.delete(key, itemType)
        itemRepository.decrementStatsCount(itemType, 1)
        itemEventRepository.create(
            ItemEvent(
                createdAt = now,
                itemType = itemType,
                itemKey = key,
                eventType = ItemEventType.Expired
            )
        )
    }
}
