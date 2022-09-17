package io.github.irgaly.kottage.internal

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
    fun addEvent(
        now: Long,
        eventType: ItemEventType,
        eventExpireTime: Duration?,
        itemType: String,
        itemKey: String,
        maxEventEntryCount: Long
    ): String {
        val id = addEventInternal(now, eventExpireTime, itemType, itemKey, eventType)
        itemEventRepository.incrementStatsCount(itemType, 1)
        reduceEvents(now, itemType, maxEventEntryCount)
        return id
    }

    /**
     * Get events
     * This should be called in transaction
     */
    fun getEvents(
        afterUnixTimeMillisAt: Long,
        itemType: String? = null,
        limit: Long? = null
    ): List<ItemEvent> {
        return itemEventRepository.selectAfter(
            createdAt = afterUnixTimeMillisAt,
            itemType = itemType,
            limit = limit
        )
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
    fun evictCaches(now: Long, itemType: String? = null) {
        if (itemType != null) {
            itemRepository.getExpiredKeys(now, itemType) { key, _ ->
                deleteItem(key, itemType)
            }
        } else {
            itemRepository.getExpiredKeys(now) { key, expiredItemType ->
                deleteItem(key, expiredItemType)
            }
        }
    }

    /**
     * Delete old events
     * This should be called in transaction
     */
    fun evictEvents(now: Long, itemType: String? = null) {
        if (itemType != null) {
            itemEventRepository.getExpiredIds(now, itemType) { id, _ ->
                deleteEvent(id, itemType)
            }
        } else {
            itemEventRepository.getExpiredIds(now) { id, type ->
                deleteEvent(id, type)
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
            deleteItem(key, itemType)
            deleted++
        }
        return deleted
    }

    /**
     * Delete item
     */
    private fun deleteItem(key: String, itemType: String) {
        itemRepository.delete(key, itemType)
        itemRepository.decrementStatsCount(itemType, 1)
    }

    /**
     * Delete event
     */
    private fun deleteEvent(id: String, itemType: String) {
        itemEventRepository.delete(id)
        itemEventRepository.decrementStatsCount(itemType, 1)
    }

    /**
     * Add Event item
     *
     * @return event id
     */
    private fun addEventInternal(
        now: Long,
        eventExpireTime: Duration?,
        itemType: String,
        itemKey: String,
        eventType: ItemEventType
    ): String {
        val id = Id.generateUuidV4Short()
        val latestCreatedAt = (itemEventRepository.getLatestCreatedAt(itemType) ?: 0)
        val createdAt = now.coerceAtLeast(latestCreatedAt + 1)
        val expireAt = eventExpireTime?.let { duration ->
            (createdAt + duration.inWholeMilliseconds)
        }
        itemEventRepository.create(
            ItemEvent(
                id = id,
                createdAt = createdAt,
                expireAt = expireAt,
                itemType = itemType,
                itemKey = itemKey,
                eventType = eventType
            )
        )
        return id
    }

    private fun reduceEvents(now: Long, itemType: String, maxEventEntryCount: Long) {
        val currentCount = itemEventRepository.getStatsCount(itemType)
        if (maxEventEntryCount < currentCount) {
            var deleted = 0L
            itemEventRepository.getExpiredIds(now, itemType) { id, _ ->
                deleteEvent(id, itemType)
                deleted++
            }
            val calculatedReduceCount = (maxEventEntryCount * 0.25).toLong().coerceAtLeast(1)
            val reduceCount = calculatedReduceCount - deleted
            if (0 < reduceCount) {
                itemEventRepository.deleteOlderEvents(itemType, reduceCount)
                val count = itemEventRepository.getCount(itemType)
                itemEventRepository.updateStatsCount(itemType, count)
            }
        }
    }
}
