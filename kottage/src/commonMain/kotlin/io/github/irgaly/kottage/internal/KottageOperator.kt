package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KottageEvent
import io.github.irgaly.kottage.internal.model.Item
import io.github.irgaly.kottage.internal.model.ItemEvent
import io.github.irgaly.kottage.internal.model.ItemEventType
import io.github.irgaly.kottage.internal.repository.KottageItemEventRepository
import io.github.irgaly.kottage.internal.repository.KottageItemRepository
import io.github.irgaly.kottage.internal.repository.KottageStatsRepository
import io.github.irgaly.kottage.platform.Id
import io.github.irgaly.kottage.strategy.KottageStrategyOperator
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Data Operation Logic
 */
internal class KottageOperator(
    private val itemRepository: KottageItemRepository,
    private val itemEventRepository: KottageItemEventRepository,
    private val statsRepository: KottageStatsRepository
): KottageStrategyOperator {
    /**
     * This should be called in transaction
     *
     * @return Event id
     */
    fun addCreateEvent(
        now: Long,
        itemType: String,
        itemKey: String,
        maxEventEntryCount: Long
    ): String {
        val id = addEvent(now, itemType, itemKey, ItemEventType.Create)
        itemEventRepository.incrementStatsCount(itemType, 1)
        reduceEvents(itemType, maxEventEntryCount)
        return id
    }

    /**
     * This should be called in transaction
     *
     * @return Event id
     */
    fun addUpdateEvent(
        now: Long,
        itemType: String,
        itemKey: String,
        maxEventEntryCount: Long
    ): String {
        val id = addEvent(now, itemType, itemKey, ItemEventType.Update)
        itemEventRepository.incrementStatsCount(itemType, 1)
        reduceEvents(itemType, maxEventEntryCount)
        return id
    }

    /**
     * This should be called in transaction
     *
     * @return Event id
     */
    fun addDeleteEvent(
        now: Long,
        itemType: String,
        itemKey: String,
        maxEventEntryCount: Long
    ): String {
        val id = addEvent(now, itemType, itemKey, ItemEventType.Delete)
        itemEventRepository.incrementStatsCount(itemType, 1)
        reduceEvents(itemType, maxEventEntryCount)
        return id
    }

    /**
     * Get events
     * This should be called in transaction
     */
    fun getEvents(itemType: String, afterUnixTimeMillisAt: Long, limit: Long?): List<KottageEvent> {
        return itemEventRepository.selectAfter(
            itemType = itemType,
            createdAt = afterUnixTimeMillisAt,
            limit = limit
        ).map {
            KottageEvent.from(it)
        }
    }

    /**
     * This should be called in transaction
     */
    fun getAutoCompactionNeeded(now: Long, duration: Duration?): Boolean {
        return if (duration != null) {
            val lastCompaction = statsRepository.getLastEvictAt()
            (duration <= (now - lastCompaction).milliseconds)
        } else false
    }

    /**
     * This should be called in transaction
     */
    fun getOrNull(key: String, itemType: String, now: Long): Item? {
        var item = itemRepository.get(key, itemType)
        if (item?.isExpired(now) == true) {
            // delete cache
            item = null
            itemRepository.delete(key, itemType)
            itemRepository.decrementStatsCount(itemType, 1)
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
                deleteExpiredItem(key, itemType)
            }
        } else {
            itemRepository.getExpiredKeys(now) { key, expiredItemType ->
                deleteExpiredItem(key, expiredItemType)
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
            deleteExpiredItem(key, itemType)
            deleted++
        }
        return deleted
    }

    /**
     * Delete expired items
     */
    private fun deleteExpiredItem(key: String, itemType: String) {
        itemRepository.delete(key, itemType)
        itemRepository.decrementStatsCount(itemType, 1)
    }

    /**
     * Add Event item
     *
     * @return event id
     */
    private fun addEvent(
        now: Long,
        itemType: String,
        itemKey: String,
        eventType: ItemEventType
    ): String {
        val latestCreatedAt = (itemEventRepository.getLatestCreatedAt(itemType) ?: 0)
        val createdAt = now.coerceAtLeast(latestCreatedAt + 1)
        val id = Id.generateUuidV4Short()
        itemEventRepository.create(
            ItemEvent(
                id = id,
                createdAt = createdAt,
                itemType = itemType,
                itemKey = itemKey,
                eventType = eventType
            )
        )
        return id
    }

    private fun reduceEvents(itemType: String, maxEventEntryCount: Long) {
        val currentCount = itemEventRepository.getStatsCount(itemType)
        if (maxEventEntryCount < currentCount) {
            // TODO: delete time base expiring first.
            val reduceCount = (maxEventEntryCount * 0.25).toLong().coerceAtLeast(1)
            itemEventRepository.deleteOlderEvents(itemType, reduceCount)
            val count = itemEventRepository.getCount(itemType)
            itemEventRepository.updateStatsCount(itemType, count)
        }
    }
}
