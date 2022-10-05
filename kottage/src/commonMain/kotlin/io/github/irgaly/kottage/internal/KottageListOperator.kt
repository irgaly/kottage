package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KottageList
import io.github.irgaly.kottage.KottageStorage
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
    private val itemRepository: KottageItemRepository,
    private val itemListRepository: KottageItemListRepository,
    private val itemEventRepository: KottageItemEventRepository,
    private val statsRepository: KottageStatsRepository
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
                itemListRepository.updateNextId(id = nextId, nextId = last.id)
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
     */
    fun incrementStatsItemCount(count: Long) {
        itemListRepository.incrementStatsCount(
            type = listType,
            count = count
        )
    }

    /**
     * This should be called in transaction
     */
    fun decrementStatsItemCount(count: Long) {
        itemListRepository.decrementStatsCount(
            type = listType,
            count = count
        )
    }

    /**
     * This should be called in transaction
     */
    fun removeListItem(positionId: String) {
        storageOperator.removeListItemInternal(positionId = positionId, listType = listType)
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
    fun updateEntryNextId(
        positionId: String,
        nextId: String?
    ) {
        itemListRepository.updateNextId(
            id = positionId,
            nextId = nextId
        )
    }

    /**
     * This should be called in transaction
     */
    fun updateEntryPreviousId(
        positionId: String,
        previousId: String?
    ) {
        itemListRepository.updatePreviousId(
            id = positionId,
            previousId = previousId
        )
    }
}
