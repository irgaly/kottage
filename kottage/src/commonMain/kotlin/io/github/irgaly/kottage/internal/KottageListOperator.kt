package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KottageList
import io.github.irgaly.kottage.KottageStorage
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
     */
    fun addInitialItem(entry: ItemListEntry) {
        itemListRepository.upsert(entry)
        itemListRepository.createStats(
            type = listType,
            count = 1,
            firstItemListEntryId = entry.id,
            lastItemListEntryId = entry.id
        )
    }

    /**
     * This should be called in transaction
     */
    fun addFirstItem(entry: ItemListEntry, currentFirstPositionId: String? = null) {
        var oldPositionId = currentFirstPositionId
        if (oldPositionId == null) {
            oldPositionId = itemListRepository.getStats(listType)?.firstItemPositionId
                ?: throw IllegalStateException("item_list_stats entry not exist: listType = $listType")
        }
        itemListRepository.updatePreviousId(id = oldPositionId, previousId = entry.id)
        itemListRepository.upsert(entry)
        itemListRepository.updateStatsFirstItem(
            type = listType,
            id = entry.id
        )
    }

    /**
     * This should be called in transaction
     */
    fun addLastItem(entry: ItemListEntry, currentLastPositionId: String? = null) {
        var oldPositionId = currentLastPositionId
        if (oldPositionId == null) {
            oldPositionId = itemListRepository.getStats(listType)?.lastItemPositionId
                ?: throw IllegalStateException("item_list_stats entry not exist: listType = $listType")
        }
        itemListRepository.updateNextId(id = oldPositionId, nextId = entry.id)
        itemListRepository.upsert(entry)
        itemListRepository.updateStatsLastItem(
            type = listType,
            id = entry.id
        )
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
