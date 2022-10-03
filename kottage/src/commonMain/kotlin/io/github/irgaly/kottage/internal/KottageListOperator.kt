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
    fun removeListItem(positionId: String) {
        storageOperator.removeListItemInternal(positionId = positionId, listType = listType)
    }
}
