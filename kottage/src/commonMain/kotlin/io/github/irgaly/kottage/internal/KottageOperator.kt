package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KottageOptions
import io.github.irgaly.kottage.internal.database.Transaction
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
import io.github.irgaly.kottage.strategy.KottageTransaction
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Data Operation Logic
 */
internal class KottageOperator(
    private val options: KottageOptions,
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
    suspend fun addEvent(
        transaction: Transaction,
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
            transaction,
            now = now,
            eventExpireTime = eventExpireTime,
            itemType = itemType,
            itemKey = itemKey,
            itemListId = itemListId,
            itemListType = itemListType,
            eventType = eventType
        )
        itemEventRepository.incrementStatsCount(transaction, itemType, 1)
        reduceEvents(transaction, now, itemType, maxEventEntryCount)
        return id
    }

    /**
     * Get events
     * This should be called in transaction
     */
    suspend fun getEvents(
        transaction: Transaction,
        afterUnixTimeMillisAt: Long,
        limit: Long? = null
    ): List<ItemEvent> {
        return itemEventRepository.selectAfter(
            transaction,
            createdAt = afterUnixTimeMillisAt,
            limit = limit
        )
    }

    /**
     * Get Item events
     * This should be called in transaction
     */
    suspend fun getItemEvents(
        transaction: Transaction,
        itemType: String,
        afterUnixTimeMillisAt: Long,
        limit: Long? = null
    ): List<ItemEvent> {
        return itemEventRepository.selectItemEventAfter(
            transaction,
            createdAt = afterUnixTimeMillisAt,
            itemType = itemType,
            limit = limit
        )
    }

    /**
     * Get List events
     * This should be called in transaction
     */
    suspend fun getListEvents(
        transaction: Transaction,
        listType: String,
        afterUnixTimeMillisAt: Long,
        limit: Long? = null
    ): List<ItemEvent> {
        return itemEventRepository.selectListEventAfter(
            transaction,
            createdAt = afterUnixTimeMillisAt,
            listType = listType,
            limit = limit
        )
    }

    /**
     * This should be called in transaction
     */
    suspend fun getAutoCompactionNeeded(transaction: Transaction, now: Long): Boolean {
        return options.autoCompactionDuration?.let { duration ->
            val lastCompaction = statsRepository.getLastEvictAt(transaction)
            (duration <= (now - lastCompaction).milliseconds)
        } ?: false
    }

    /**
     * This should be called in transaction
     */
    suspend fun getAllListType(transaction: Transaction, receiver: suspend (listType: String) -> Unit) {
        itemListRepository.getAllTypes(transaction, receiver)
    }

    /**
     * Delete expired items
     * existing items in ItemList are ignored
     * This should be called in transaction
     */
    suspend fun evictCaches(transaction: Transaction, now: Long, itemType: String? = null) {
        if (itemType != null) {
            itemRepository.getExpiredKeys(transaction, now, itemType) { key, _ ->
                val itemListEntryIds = itemListRepository.getIds(transaction, itemType = itemType, itemKey = key)
                if (itemListEntryIds.isEmpty()) {
                    // ItemList に存在しなければ削除可能
                    deleteItemInternal(transaction, key, itemType)
                }
            }
        } else {
            itemRepository.getExpiredKeys(transaction, now) { key, expiredItemType ->
                val itemListEntryIds =
                    itemListRepository.getIds(transaction, itemType = expiredItemType, itemKey = key)
                if (itemListEntryIds.isEmpty()) {
                    // ItemList に存在しなければ削除可能
                    deleteItemInternal(transaction, key, expiredItemType)
                }
            }
        }
    }

    /**
     * Delete old events
     * This should be called in transaction
     */
    suspend fun evictEvents(transaction: Transaction, now: Long, itemType: String? = null) {
        itemEventRepository.deleteExpiredEvents(transaction, now, itemType) { _, type ->
            itemEventRepository.decrementStatsCount(transaction, type, 1)
        }
    }

    /**
     * This should be called in transaction
     */
    suspend fun evictEmptyStats(transaction: Transaction) {
        val limit = 100L
        var hasNext = true
        while (hasNext) {
            val statsList = itemRepository.getEmptyStats(transaction, limit = limit)
            statsList.forEach { stats ->
                itemRepository.deleteStats(transaction, stats.itemType)
            }
            hasNext = (limit <= statsList.size)
        }
    }

    /**
     * This should be called in transaction
     */
    suspend fun evictExpiredListEntries(transaction: Transaction, now: Long, beforeExpireAt: Long?, listType: String? = null) {
        suspend fun evict(listType: String) {
            invalidateExpiredListEntries(transaction, now = now, listType = listType)
            var limit = 100L
            while (0 < limit) {
                val invalidatedIds = itemListRepository.getInvalidatedItemIds(
                    transaction,
                    type = listType,
                    beforeExpireAt = beforeExpireAt,
                    limit = limit
                )
                if (invalidatedIds.isNotEmpty()) {
                    removeListEntries(transaction, listType = listType, positionIds = invalidatedIds)
                }
                if (invalidatedIds.size < limit) {
                    limit = 0
                }
            }
        }
        if (listType != null) {
            evict(listType = listType)
        } else {
            getAllListType(transaction) { evict(listType = it) }
        }
    }

    /**
     * This should be called in transaction
     */
    suspend fun getListStats(transaction: Transaction, listType: String): ItemListStats? {
        return itemListRepository.getStats(transaction, type = listType)
    }

    /**
     * Get List Item Count
     * This should be called in transaction
     */
    suspend fun getListCount(transaction: Transaction, listType: String, now: Long): Long {
        invalidateExpiredListEntries(transaction, now = now, listType = listType)
        return itemListRepository.getStatsCount(transaction, type = listType)
    }

    /**
     * This should be called in transaction
     *
     * 前後のアイテムと item_list_stats の先頭末尾情報を更新しながらアイテムを取り除く
     * 有効なアイテムを削除した場合は Delete Event を追加する
     */
    suspend fun removeListEntries(transaction: Transaction, listType: String, positionIds: List<String>) {
        val entries = positionIds.map {
            itemListRepository.get(transaction, it)
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
                itemListRepository.delete(transaction, entry.id)
            }
            if (newPreviousIds.isEmpty() && newNextIds.isEmpty()) {
                // アップデート対象が存在しない = List が空になった
                itemListRepository.deleteStats(transaction, type = listType)
            } else {
                newPreviousIds.forEach { (id, previousId) ->
                    itemListRepository.updatePreviousId(transaction, id = id, previousId = previousId)
                }
                newNextIds.forEach { (id, nextId) ->
                    itemListRepository.updateNextId(transaction, id = id, nextId = nextId)
                }
                if (newFirstId != null) {
                    itemListRepository.updateStatsFirstItem(
                        transaction,
                        type = listType,
                        id = newFirstId
                    )
                }
                if (newLastId != null) {
                    itemListRepository.updateStatsLastItem(
                        transaction,
                        type = listType,
                        id = newLastId
                    )
                }
                if (0 < deleted) {
                    itemListRepository.decrementStatsCount(
                        transaction,
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
    suspend fun invalidateExpiredListEntries(transaction: Transaction, now: Long, listType: String? = null) {
        suspend fun invalidate(listType: String) {
            itemListRepository.getStats(transaction, type = listType)?.let { stats ->
                var invalidated = 0L
                val scanInvalidate: suspend (startPositionId: String, block: (ItemListEntry) -> String?) -> Unit = { startPositionId, block ->
                    var nextPositionId: String? = startPositionId
                    while ((nextPositionId != null) && (invalidated < stats.count)) {
                        val entry = checkNotNull(itemListRepository.get(transaction, nextPositionId))
                        val expired = entry.isExpired(now)
                        if (entry.itemExists && expired) {
                            itemListRepository.removeItemKey(transaction, entry.id)
                            itemListRepository.removeUserData(transaction, entry.id)
                            invalidated++
                        }
                        nextPositionId = if (entry.itemExists && !expired) null else block(entry)
                    }
                }
                scanInvalidate(stats.firstItemPositionId) { it.nextId }
                scanInvalidate(stats.lastItemPositionId) { it.previousId }
                if (0 < invalidated) {
                    itemListRepository.decrementStatsCount(
                        transaction,
                        type = listType,
                        count = invalidated
                    )
                }
            }
        }
        if (listType != null) {
            invalidate(listType = listType)
        } else {
            getAllListType(transaction) { invalidate(listType = it) }
        }
    }

    /**
     * This should be called in transaction
     */
    suspend fun updateLastEvictAt(transaction: Transaction, now: Long) {
        statsRepository.updateLastEvictAt(transaction, now)
    }

    override suspend fun updateItemLastRead(transaction: KottageTransaction, key: String, itemType: String, now: Long) {
        itemRepository.updateLastRead(transaction.transaction, key, itemType, now)
    }

    override suspend fun deleteLeastRecentlyUsed(transaction: KottageTransaction, itemType: String, limit: Long) {
        var deleted = 0L
        itemRepository.getLeastRecentlyUsedKeys(transaction.transaction, itemType, null) { key ->
            val itemListEntryIds = itemListRepository.getIds(transaction.transaction, itemType = itemType, itemKey = key)
            if (itemListEntryIds.isEmpty()) {
                // ItemList に存在しなければ削除可能
                deleteItemInternal(transaction.transaction, key, itemType)
                deleted++
            }
            (deleted < limit)
        }
    }

    override suspend fun deleteOlderItems(transaction: KottageTransaction, itemType: String, limit: Long) {
        var deleted = 0L
        itemRepository.getOlderKeys(transaction.transaction, itemType, null) { key ->
            val itemListEntryIds = itemListRepository.getIds(transaction.transaction, itemType = itemType, itemKey = key)
            if (itemListEntryIds.isEmpty()) {
                // ItemList に存在しなければ削除可能
                deleteItemInternal(transaction.transaction, key, itemType)
                deleted++
            }
            (deleted < limit)
        }
    }

    override suspend fun deleteExpiredItems(transaction: KottageTransaction, itemType: String, now: Long): Long {
        var deleted = 0L
        // List Invalidate を処理しておく
        invalidateExpiredListEntries(transaction.transaction, now)
        itemRepository.getExpiredKeys(transaction.transaction, now, itemType) { key, _ ->
            val itemListEntryIds = itemListRepository.getIds(transaction.transaction, itemType = itemType, itemKey = key)
            if (itemListEntryIds.isEmpty()) {
                // ItemList に存在しなければ削除可能
                deleteItemInternal(transaction.transaction, key, itemType)
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
    suspend fun deleteItemInternal(transaction: Transaction, key: String, itemType: String) {
        itemRepository.delete(transaction, key, itemType)
        itemRepository.decrementStatsCount(transaction, itemType, 1)
    }

    /**
     * This should be called in transaction
     */
    suspend fun deleteItemStats(transaction: Transaction, itemType: String) {
        itemRepository.deleteStats(transaction, itemType = itemType)
    }

    /**
     * Add Event item
     *
     * This should be called in transaction
     *
     * @return event id
     */
    private suspend fun addEventInternal(
        transaction: Transaction,
        now: Long,
        eventExpireTime: Duration?,
        itemType: String,
        itemKey: String,
        itemListId: String?,
        itemListType: String?,
        eventType: ItemEventType
    ): String {
        val id = Id.generateUuidV4Short()
        val latestCreatedAt = (itemEventRepository.getLatestCreatedAt(transaction) ?: 0)
        val createdAt = now.coerceAtLeast(latestCreatedAt + 1)
        val expireAt = eventExpireTime?.let { duration ->
            (createdAt + duration.inWholeMilliseconds)
        }
        itemEventRepository.create(
            transaction,
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
    private suspend fun reduceEvents(transaction: Transaction, now: Long, itemType: String, maxEventEntryCount: Long) {
        val currentCount = itemEventRepository.getStatsCount(transaction, itemType)
        if (maxEventEntryCount < currentCount) {
            val deleted = itemEventRepository.deleteExpiredEvents(transaction, now, itemType)
            itemEventRepository.decrementStatsCount(transaction, itemType, deleted)
            val calculatedReduceCount = (maxEventEntryCount * 0.25).toLong().coerceAtLeast(1)
            val reduceCount = calculatedReduceCount - deleted
            if (0 < reduceCount) {
                itemEventRepository.deleteOlderEvents(transaction, itemType, reduceCount)
                val count = itemEventRepository.getCount(transaction, itemType)
                itemEventRepository.updateStatsCount(transaction, itemType, count)
            }
        }
    }
}
