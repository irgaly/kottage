package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KottageList
import io.github.irgaly.kottage.KottageListDirection
import io.github.irgaly.kottage.KottageStorage
import io.github.irgaly.kottage.internal.model.Item
import io.github.irgaly.kottage.internal.model.ItemEventType
import io.github.irgaly.kottage.internal.model.ItemListEntry
import io.github.irgaly.kottage.internal.repository.KottageItemEventRepository
import io.github.irgaly.kottage.internal.repository.KottageItemListRepository
import io.github.irgaly.kottage.internal.repository.KottageItemRepository
import io.github.irgaly.kottage.internal.repository.KottageStatsRepository

/**
 * Database Operation Logic of Kottage List
 */
internal class KottageListOperator(
    private val kottageList: KottageList,
    private val storage: KottageStorage,
    private val operator: KottageOperator,
    private val storageOperator: KottageStorageOperator,
    @Suppress("unused") private val itemRepository: KottageItemRepository,
    private val itemListRepository: KottageItemListRepository,
    @Suppress("unused") private val itemEventRepository: KottageItemEventRepository,
    @Suppress("unused") private val statsRepository: KottageStatsRepository
) {
    private val listType = kottageList.name

    /**
     * This should be called in transaction
     *
     * 前後のアイテムと item_list_stats の先頭末尾情報を更新しながらアイテムを追加する
     *
     * @param entries previousId, nextId が正しく設定された Entry
     */
    fun addListEntries(entries: List<ItemListEntry>, now: Long) {
        if (entries.isNotEmpty()) {
            val hasStats = (itemListRepository.getStats(type = listType) != null)
            val first = entries.first()
            val last = entries.last()
            entries.forEach { entry ->
                itemListRepository.upsert(entry)
                operator.addEvent(
                    now = now,
                    eventType = ItemEventType.Create,
                    eventExpireTime = storage.options.eventExpireTime,
                    itemType = entry.itemType,
                    itemKey = checkNotNull(entry.itemKey),
                    itemListId = entry.id,
                    itemListType = listType,
                    maxEventEntryCount = storage.options.maxEventEntryCount
                )
            }
            first.previousId?.let { previousId ->
                itemListRepository.updateNextId(id = previousId, nextId = first.id)
            }
            last.nextId?.let { nextId ->
                itemListRepository.updatePreviousId(id = nextId, previousId = last.id)
            }
            if (hasStats) {
                if (first.isFirst) {
                    itemListRepository.updateStatsFirstItem(
                        type = listType,
                        id = first.id
                    )
                }
                if (last.isLast) {
                    itemListRepository.updateStatsLastItem(
                        type = listType,
                        id = last.id
                    )
                }
                itemListRepository.incrementStatsCount(
                    type = listType,
                    count = entries.size.toLong()
                )
            } else {
                itemListRepository.createStats(
                    type = listType,
                    count = entries.size.toLong(),
                    firstItemListEntryId = entries.first().id,
                    lastItemListEntryId = entries.last().id
                )
            }
        }
    }

    /**
     * This should be called in transaction
     *
     * positionId の ItemListEntry を取得する
     */
    fun getListItem(
        positionId: String
    ): ItemListEntry? {
        return itemListRepository.get(positionId)?.let { entry ->
            if (entry.type == listType) entry else null
        }
    }

    /**
     * This should be called in transaction
     *
     * positionId からたどり、有効な Entry があればそれを返す
     */
    fun getAvailableListItem(
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
     */
    fun removeListItem(positionId: String): Boolean {
        val exists = (itemListRepository.get(positionId) != null)
        if (exists) {
            storageOperator.removeListItemInternal(positionId = positionId, listType = listType)
        }
        return exists
    }

    /**
     * This should be called in transaction
     */
    fun getFirstItemPositionId(): String? {
        return itemListRepository.getStats(listType)?.firstItemPositionId
    }

    /**
     * This should be called in transaction
     */
    fun getLastItemPositionId(): String? {
        return itemListRepository.getStats(listType)?.lastItemPositionId
    }

    /**
     * This should be called in transaction
     */
    fun updateItemKey(
        positionId: String,
        item: Item,
        now: Long
    ) {
        itemListRepository.updateItemKey(
            id = positionId,
            itemType = item.type,
            itemKey = item.key,
            expireAt = kottageList.options.itemExpireTime?.let { duration ->
                now + duration.inWholeMilliseconds
            }
        )
        operator.addEvent(
            now = now,
            eventType = ItemEventType.Update,
            eventExpireTime = storage.options.eventExpireTime,
            itemType = item.type,
            itemKey = item.key,
            itemListId = positionId,
            itemListType = listType,
            maxEventEntryCount = storage.options.maxEventEntryCount
        )
    }

    /**
     * This should be called in transaction
     *
     * * リスト全体から削除可能な entity を取り除く
     */
    fun evictExpiredEntries(now: Long) {
        operator.evictExpiredListEntries(
            listType = listType,
            now = now,
            beforeExpireAt = null
        )
    }

    /**
     * This should be called in transaction
     */
    fun invalidateExpiredListEntries(now: Long) {
        operator.invalidateExpiredListEntries(listType = listType, now = now)
    }

    /**
     * This should be called in transaction
     */
    fun clear() {
        itemListRepository.deleteAll(type = listType)
        itemListRepository.deleteStats(type = listType)
        itemEventRepository.deleteAllList(listType = listType)
    }

    /**
     * This should be called in transaction
     */
    fun getDebugStatus(): String {
        val stats = itemListRepository.getStats(type = listType)
        val invalidatedItemsCount = itemListRepository.getInvalidatedItemCount(type = listType)
        val itemsCount = itemListRepository.getCount(type = listType)
        return """
        available items: ${stats?.count ?: "(no stats)"}
        invalidated items: $invalidatedItemsCount
        total = $itemsCount
        """.trimIndent()
    }

    /**
     * This should be called in transaction
     */
    fun getDebugListRawData(): String {
        val data = StringBuilder()
        val stats = itemListRepository.getStats(type = listType)
        if (stats != null) {
            var nextId: String? = stats.firstItemPositionId
            data.appendLine("[first] ${stats.firstItemPositionId}")
            while (nextId != null) {
                val entry = itemListRepository.get(nextId)
                if (entry != null) {
                    val item = entry.itemKey?.let { itemKey ->
                        itemRepository.get(key = itemKey, itemType = entry.itemType)
                    }
                    data.appendLine(
                        "-> [${entry.id} : expireAt = ${entry.expireAt}]"
                                + (item?.let {
                            " => [${item.key} : expireAt = ${item.expireAt}]"
                        } ?: "")
                    )
                }
                nextId = entry?.nextId
            }
            data.appendLine("[last] ${stats.lastItemPositionId}")
        } else {
            data.appendLine("(no stats)")
        }
        return data.toString()
    }
}
