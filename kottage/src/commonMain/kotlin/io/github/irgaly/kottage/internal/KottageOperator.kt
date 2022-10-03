package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KottageListDirection
import io.github.irgaly.kottage.internal.model.ItemEvent
import io.github.irgaly.kottage.internal.model.ItemEventType
import io.github.irgaly.kottage.internal.model.ItemListEntry
import io.github.irgaly.kottage.internal.model.ItemListStats
import io.github.irgaly.kottage.internal.repository.KottageItemEventRepository
import io.github.irgaly.kottage.internal.repository.KottageItemListRepository
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
    private val itemListRepository: KottageItemListRepository,
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
        itemListId: String?,
        itemListType: String?,
        maxEventEntryCount: Long
    ): String {
        val id = addEventInternal(
            now = now,
            eventExpireTime = eventExpireTime,
            itemType = itemType,
            itemKey = itemKey,
            itemListId = itemListId,
            itemListType = itemListType,
            eventType = eventType
        )
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
     * Delete expired items
     * existing items in ItemList are ignored
     * This should be called in transaction
     */
    fun evictCaches(now: Long, itemType: String? = null) {
        if (itemType != null) {
            itemRepository.getExpiredKeys(now, itemType) { key, _ ->
                val itemListEntryIds = itemListRepository.getIds(itemType = itemType, itemKey = key)
                if (itemListEntryIds.isEmpty()) {
                    // ItemList に存在しなければ削除可能
                    deleteItemInternal(key, itemType)
                }
            }
        } else {
            itemRepository.getExpiredKeys(now) { key, expiredItemType ->
                val itemListEntryIds =
                    itemListRepository.getIds(itemType = expiredItemType, itemKey = key)
                if (itemListEntryIds.isEmpty()) {
                    // ItemList に存在しなければ削除可能
                    deleteItemInternal(key, expiredItemType)
                }
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


    /**
     * This should be called in transaction
     */
    fun getListStats(listType: String): ItemListStats? {
        return itemListRepository.getStats(type = listType)
    }

    /**
     * Get List Item Count
     * This should be called in transaction
     */
    fun getListCount(listType: String): Long {
        return itemListRepository.getStatsCount(type = listType)
    }

    /**
     * This should be called in transaction
     *
     * positionId からたどり、有効な Entry があればそれを返す
     */
    fun getAvailableListItem(
        listType: String,
        positionId: String,
        direction: KottageListDirection
    ): ItemListEntry? {
        var nextId: String? = positionId
        var entry: ItemListEntry? = null
        while (entry == null && nextId != null) {
            val current = itemListRepository.get(nextId)
            nextId = when (direction) {
                KottageListDirection.Forward -> current?.nextId
                KottageListDirection.Backward -> current?.previousId
            }
            if (current != null) {
                if (current.type != listType) {
                    // positionId と listType　が不一致のとき
                    nextId = null
                } else if (current.itemExists) {
                    entry = current
                }
            }
        }
        return entry
    }

    /**
     * This should be called in transaction
     *
     * positionId の ItemListEntry を取得する
     */
    fun getListItem(
        listType: String,
        positionId: String
    ): ItemListEntry? {
        return itemListRepository.get(positionId)
    }

    override fun updateItemLastRead(key: String, itemType: String, now: Long) {
        itemRepository.updateLastRead(key, itemType, now)
    }

    override fun deleteLeastRecentlyUsed(itemType: String, limit: Long) {
        itemRepository.getLeastRecentlyUsedKeys(itemType, limit) { key, _ ->
            val itemListEntryIds = itemListRepository.getIds(itemType = itemType, itemKey = key)
            if (itemListEntryIds.isEmpty()) {
                // ItemList に存在しなければ削除可能
                deleteItemInternal(key, itemType)
            }
        }
    }

    override fun deleteOlderItems(itemType: String, limit: Long) {
        itemRepository.getOlderKeys(itemType, limit) { key, _ ->
            val itemListEntryIds = itemListRepository.getIds(itemType = itemType, itemKey = key)
            if (itemListEntryIds.isEmpty()) {
                // ItemList に存在しなければ削除可能
                deleteItemInternal(key, itemType)
            }
        }
    }

    override fun deleteExpiredItems(itemType: String, now: Long): Long {
        var deleted = 0L
        itemRepository.getExpiredKeys(now, itemType) { key, _ ->
            val itemListEntryIds = itemListRepository.getIds(itemType = itemType, itemKey = key)
            if (itemListEntryIds.isEmpty()) {
                // ItemList に存在しなければ削除可能
                deleteItemInternal(key, itemType)
                deleted++
            }
        }
        return deleted
    }

    /**
     * Delete item
     * This should be called in transaction
     *
     * ItemList の存在チェックなしで Item を削除する
     */
    fun deleteItemInternal(key: String, itemType: String) {
        itemRepository.delete(key, itemType)
        itemRepository.decrementStatsCount(itemType, 1)
    }

    /**
     * Delete event
     *
     * This should be called in transaction
     */
    private fun deleteEvent(id: String, itemType: String) {
        itemEventRepository.delete(id)
        itemEventRepository.decrementStatsCount(itemType, 1)
    }

    /**
     * Add Event item
     *
     * This should be called in transaction
     *
     * @return event id
     */
    private fun addEventInternal(
        now: Long,
        eventExpireTime: Duration?,
        itemType: String,
        itemKey: String,
        itemListId: String?,
        itemListType: String?,
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
                itemListId = itemListId,
                itemListType = itemListType,
                eventType = eventType
            )
        )
        return id
    }

    /**
     * This should be called in transaction
     */
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
