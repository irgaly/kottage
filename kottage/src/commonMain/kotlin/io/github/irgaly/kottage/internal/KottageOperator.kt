package io.github.irgaly.kottage.internal

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
     * This should be called in transaction
     */
    fun getAllListType(receiver: (listType: String) -> Unit) {
        itemListRepository.getAllTypes(receiver)
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
    fun evictExpiredListEntries(now: Long, beforeExpireAt: Long?, listType: String? = null) {
        fun evict(listType: String) {
            invalidateExpiredListEntries(now = now, listType = listType)
            var limit = 100L
            while (0 < limit) {
                val invalidatedIds = itemListRepository.getInvalidatedItemIds(
                    type = listType,
                    beforeExpireAt = beforeExpireAt,
                    limit = limit
                )
                if (invalidatedIds.isNotEmpty()) {
                    removeListEntries(listType = listType, positionIds = invalidatedIds)
                }
                if (invalidatedIds.size < limit) {
                    limit = 0
                }
            }
        }
        if (listType != null) {
            evict(listType = listType)
        } else {
            getAllListType { evict(listType = it) }
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
    fun getListCount(listType: String, now: Long): Long {
        invalidateExpiredListEntries(now = now, listType = listType)
        return itemListRepository.getStatsCount(type = listType)
    }

    /**
     * This should be called in transaction
     *
     * 前後のアイテムと item_list_stats の先頭末尾情報を更新しながらアイテムを取り除く
     * 有効なアイテムを削除した場合は Delete Event を追加する
     */
    fun removeListEntries(listType: String, positionIds: List<String>) {
        val entries = positionIds.map {
            itemListRepository.get(it)
        }.mapNotNull {
            if (it?.type == listType) {
                it.id to it
            } else null
        }.toMap()
        if (entries.isNotEmpty()) {
            val newPreviousIds = mutableMapOf<String, String?>()
            val newNextIds = mutableMapOf<String, String?>()
            var newFirstId: String? = null
            var newLastId: String? = null
            var deleted = 0L
            val remains = entries.toMutableMap()
            while (remains.isNotEmpty()) {
                // entries を取り除くために必要な更新アイテムを見つける
                var previousId: String? = null
                var nextId: String? = null
                remains.remove(remains.keys.first())?.let {
                    previousId = it.previousId
                    nextId = it.nextId
                }
                while (entries.containsKey(previousId)) {
                    remains.remove(previousId)
                    previousId = entries[previousId]?.previousId
                }
                while (entries.containsKey(nextId)) {
                    remains.remove(nextId)
                    nextId = entries[nextId]?.nextId
                }
                when (val id = previousId) {
                    null -> newFirstId = nextId
                    else -> newNextIds[id] = nextId
                }
                when (val id = nextId) {
                    null -> newLastId = previousId
                    else -> newPreviousIds[id] = previousId
                }
            }
            entries.values.forEach { entry ->
                if (entry.itemExists) {
                    deleted++
                }
                itemListRepository.delete(entry.id)
            }
            if (newPreviousIds.isEmpty() && newNextIds.isEmpty()) {
                // アップデート対象が存在しない = List が空になった
                itemListRepository.deleteStats(type = listType)
            } else {
                newPreviousIds.forEach { (id, previousId) ->
                    itemListRepository.updatePreviousId(id = id, previousId = previousId)
                }
                newNextIds.forEach { (id, nextId) ->
                    itemListRepository.updateNextId(id = id, nextId = nextId)
                }
                if (newFirstId != null) {
                    itemListRepository.updateStatsFirstItem(
                        type = listType,
                        id = newFirstId
                    )
                }
                if (newLastId != null) {
                    itemListRepository.updateStatsLastItem(
                        type = listType,
                        id = newLastId
                    )
                }
                if (0 < deleted) {
                    itemListRepository.decrementStatsCount(
                        type = listType,
                        count = deleted
                    )
                }
            }
        }
    }

    /**
     * This should be called in transaction
     *
     * * リストの先頭から、expired な entity を invalidate する
     *     * 非削除対象の entity が現れたら処理を止める
     * * リストの末尾から、expired な entity を invalidate する
     *     * 非削除対象の entity が現れたら処理を止める
     */
    fun invalidateExpiredListEntries(now: Long, listType: String? = null) {
        fun invalidate(listType: String) {
            itemListRepository.getStats(type = listType)?.let { stats ->
                var invalidated = 0L
                val scanInvalidate = { startPositionId: String, block: (ItemListEntry) -> String? ->
                    var nextPositionId: String? = startPositionId
                    while ((nextPositionId != null) && (invalidated < stats.count)) {
                        val entry = checkNotNull(itemListRepository.get(nextPositionId))
                        val expired = entry.isExpired(now)
                        if (entry.itemExists && expired) {
                            itemListRepository.removeItemKey(entry.id)
                            itemListRepository.removeUserData(entry.id)
                            invalidated++
                        }
                        nextPositionId = if (entry.itemExists && !expired) null else block(entry)
                    }
                }
                scanInvalidate(stats.firstItemPositionId) { it.nextId }
                scanInvalidate(stats.lastItemPositionId) { it.previousId }
                if (0 < invalidated) {
                    itemListRepository.decrementStatsCount(
                        type = listType,
                        count = invalidated
                    )
                }
            }
        }
        if (listType != null) {
            invalidate(listType = listType)
        } else {
            getAllListType { invalidate(listType = it) }
        }
    }

    /**
     * This should be called in transaction
     */
    fun updateLastEvictAt(now: Long) {
        statsRepository.updateLastEvictAt(now)
    }

    override fun updateItemLastRead(key: String, itemType: String, now: Long) {
        itemRepository.updateLastRead(key, itemType, now)
    }

    override fun deleteLeastRecentlyUsed(itemType: String, limit: Long) {
        var deleted = 0L
        itemRepository.getLeastRecentlyUsedKeys(itemType, null) { key ->
            val itemListEntryIds = itemListRepository.getIds(itemType = itemType, itemKey = key)
            if (itemListEntryIds.isEmpty()) {
                // ItemList に存在しなければ削除可能
                deleteItemInternal(key, itemType)
                deleted++
            }
            (deleted < limit)
        }
    }

    override fun deleteOlderItems(itemType: String, limit: Long) {
        var deleted = 0L
        itemRepository.getOlderKeys(itemType, null) { key ->
            val itemListEntryIds = itemListRepository.getIds(itemType = itemType, itemKey = key)
            if (itemListEntryIds.isEmpty()) {
                // ItemList に存在しなければ削除可能
                deleteItemInternal(key, itemType)
                deleted++
            }
            (deleted < limit)
        }
    }

    override fun deleteExpiredItems(itemType: String, now: Long): Long {
        var deleted = 0L
        // List Invalidate を処理しておく
        invalidateExpiredListEntries(now)
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
