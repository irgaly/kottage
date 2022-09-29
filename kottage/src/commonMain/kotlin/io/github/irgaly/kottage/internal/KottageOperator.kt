package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KottageStorageOptions
import io.github.irgaly.kottage.internal.model.Item
import io.github.irgaly.kottage.internal.model.ItemEvent
import io.github.irgaly.kottage.internal.model.ItemEventType
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
     * Delete Item
     * This should be called in transaction
     *
     * * ItemList からも削除される
     * * ItemList / Item の Delete Event が登録される
     */
    fun deleteItem(
        key: String,
        itemType: String,
        now: Long,
        options: KottageStorageOptions,
        onEventCreated: (eventId: String) -> Unit
    ) {
        itemListRepository.getIds(
            itemType = itemType,
            itemKey = key
        ).forEach { itemListEntryId ->
            val entry = checkNotNull(itemListRepository.get(itemListEntryId))
            // ItemList から削除
            itemListRepository.updateItemKey(
                id = entry.id,
                itemKey = null
            )
            itemListRepository.decrementStatsCount(entry.type, 1)
            val eventId = addEvent(
                now = now,
                eventType = ItemEventType.Delete,
                eventExpireTime = options.eventExpireTime,
                itemType = itemType,
                itemKey = key,
                itemListId = entry.id,
                itemListType = entry.type,
                maxEventEntryCount = options.maxEventEntryCount
            )
            onEventCreated(eventId)
        }
        deleteItemInternal(key = key, itemType = itemType)
        val eventId = addEvent(
            now = now,
            eventType = ItemEventType.Delete,
            eventExpireTime = options.eventExpireTime,
            itemType = itemType,
            itemKey = key,
            itemListId = null,
            itemListType = null,
            maxEventEntryCount = options.maxEventEntryCount
        )
        onEventCreated(eventId)
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
     *
     * ItemList の存在チェックなしで Item を削除する
     */
    private fun deleteItemInternal(key: String, itemType: String) {
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
