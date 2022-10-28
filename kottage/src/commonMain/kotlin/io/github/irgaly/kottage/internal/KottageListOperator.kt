package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KottageListDirection
import io.github.irgaly.kottage.KottageListOptions
import io.github.irgaly.kottage.KottageStorageOptions
import io.github.irgaly.kottage.internal.database.Transaction
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
    private val itemType: String,
    private val listType: String,
    private val listOptions: KottageListOptions,
    private val storageOptions: KottageStorageOptions,
    private val operator: KottageOperator,
    private val storageOperator: KottageStorageOperator,
    @Suppress("unused") private val itemRepository: KottageItemRepository,
    private val itemListRepository: KottageItemListRepository,
    @Suppress("unused") private val itemEventRepository: KottageItemEventRepository,
    @Suppress("unused") private val statsRepository: KottageStatsRepository
) {
    /**
     * This should be called in transaction
     *
     * 前後のアイテムと item_list_stats の先頭末尾情報を更新しながらアイテムを追加する
     *
     * @param entries previousId, nextId が正しく設定された Entry
     */
    fun addListEntries(
        transaction: Transaction,
        entries: List<ItemListEntry>,
        now: Long
    ) {
        if (entries.isNotEmpty()) {
            val hasStats = (itemListRepository.getStats(transaction, type = listType) != null)
            val first = entries.first()
            val last = entries.last()
            entries.forEach { entry ->
                itemListRepository.upsert(transaction, entry)
                operator.addEvent(
                    transaction,
                    now = now,
                    eventType = ItemEventType.Create,
                    eventExpireTime = storageOptions.eventExpireTime,
                    itemType = entry.itemType,
                    itemKey = checkNotNull(entry.itemKey),
                    itemListId = entry.id,
                    itemListType = listType,
                    maxEventEntryCount = storageOptions.maxEventEntryCount
                )
            }
            first.previousId?.let { previousId ->
                itemListRepository.updateNextId(
                    transaction,
                    id = previousId,
                    nextId = first.id
                )
            }
            last.nextId?.let { nextId ->
                itemListRepository.updatePreviousId(
                    transaction,
                    id = nextId,
                    previousId = last.id
                )
            }
            if (hasStats) {
                if (first.isFirst) {
                    itemListRepository.updateStatsFirstItem(
                        transaction,
                        type = listType,
                        id = first.id
                    )
                }
                if (last.isLast) {
                    itemListRepository.updateStatsLastItem(
                        transaction,
                        type = listType,
                        id = last.id
                    )
                }
                itemListRepository.incrementStatsCount(
                    transaction,
                    type = listType,
                    count = entries.size.toLong()
                )
            } else {
                itemListRepository.createStats(
                    transaction,
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
        transaction: Transaction,
        positionId: String
    ): ItemListEntry? {
        return itemListRepository.get(transaction, positionId)?.let { entry ->
            if (entry.type == listType) entry else null
        }
    }

    /**
     * This should be called in transaction
     *
     * positionId からたどり、有効な Entry があればそれを返す
     */
    fun getAvailableListEntry(
        transaction: Transaction,
        positionId: String,
        direction: KottageListDirection
    ): ItemListEntry? {
        var nextId: String? = positionId
        var entry: ItemListEntry? = null
        while (entry == null && nextId != null) {
            val current = itemListRepository.get(transaction, nextId)
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
    fun removeListItem(
        transaction: Transaction,
        positionId: String,
        now: Long
    ): Boolean {
        val entry = itemListRepository.get(transaction, positionId)
        return if ((entry != null) &&
            entry.itemExists &&
            (entry.type == listType)
        ) {
            val itemKey = checkNotNull(entry.itemKey)
            storageOperator.removeListItemInternal(
                transaction,
                positionId = positionId,
                listType = listType,
                now = now,
                entry = entry
            )
            operator.addEvent(
                transaction,
                now = now,
                eventType = ItemEventType.Delete,
                eventExpireTime = storageOptions.eventExpireTime,
                itemType = entry.itemType,
                itemKey = itemKey,
                itemListId = entry.id,
                itemListType = entry.type,
                maxEventEntryCount = storageOptions.maxEventEntryCount
            )
            true
        } else false
    }

    /**
     * This should be called in transaction
     */
    fun getFirstItemPositionId(transaction: Transaction): String? {
        return itemListRepository.getStats(transaction, listType)?.firstItemPositionId
    }

    /**
     * This should be called in transaction
     */
    fun getLastItemPositionId(transaction: Transaction): String? {
        return itemListRepository.getStats(transaction, listType)?.lastItemPositionId
    }

    /**
     * This should be called in transaction
     */
    fun updateItemKey(
        transaction: Transaction,
        positionId: String,
        item: Item,
        now: Long
    ) {
        itemListRepository.updateItemKey(
            transaction,
            id = positionId,
            itemType = item.type,
            itemKey = item.key,
            expireAt = listOptions.itemExpireTime?.let { duration ->
                now + duration.inWholeMilliseconds
            }
        )
        operator.addEvent(
            transaction,
            now = now,
            eventType = ItemEventType.Update,
            eventExpireTime = storageOptions.eventExpireTime,
            itemType = item.type,
            itemKey = item.key,
            itemListId = positionId,
            itemListType = listType,
            maxEventEntryCount = storageOptions.maxEventEntryCount
        )
    }

    /**
     * This should be called in transaction
     *
     * * リスト全体から削除可能な entity を取り除く
     */
    fun evictExpiredEntries(
        transaction: Transaction,
        now: Long
    ) {
        operator.evictExpiredListEntries(
            transaction,
            now = now,
            beforeExpireAt = null,
            listType = listType
        )
    }

    /**
     * This should be called in transaction
     */
    fun invalidateExpiredListEntries(
        transaction: Transaction,
        now: Long
    ) {
        operator.invalidateExpiredListEntries(transaction, now = now, listType = listType)
    }

    /**
     * This should be called in transaction
     */
    fun clear(transaction: Transaction) {
        itemListRepository.deleteAll(transaction, type = listType)
        itemListRepository.deleteStats(transaction, type = listType)
        itemEventRepository.deleteAllList(transaction, listType = listType)
    }

    /**
     * This should be called in transaction
     */
    fun getDebugStatus(transaction: Transaction): String {
        val stats = itemListRepository.getStats(transaction, type = listType)
        val invalidatedItemsCount =
            itemListRepository.getInvalidatedItemCount(transaction, type = listType)
        val itemsCount = itemListRepository.getCount(transaction, type = listType)
        return """
        available items: ${stats?.count ?: "(no stats)"}
        invalidated items: $invalidatedItemsCount
        total = $itemsCount
        """.trimIndent()
    }

    /**
     * This should be called in transaction
     */
    fun getDebugListRawData(transaction: Transaction): String {
        val data = StringBuilder()
        val stats = itemListRepository.getStats(transaction, type = listType)
        if (stats != null) {
            var nextId: String? = stats.firstItemPositionId
            data.appendLine("[first] ${stats.firstItemPositionId}")
            while (nextId != null) {
                val entry = itemListRepository.get(transaction, nextId)
                if (entry != null) {
                    val item = entry.itemKey?.let { itemKey ->
                        itemRepository.get(
                            transaction,
                            key = itemKey,
                            itemType = entry.itemType
                        )
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
