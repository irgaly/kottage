package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KottageStorage
import io.github.irgaly.kottage.internal.model.Item
import io.github.irgaly.kottage.internal.model.ItemEventType
import io.github.irgaly.kottage.internal.repository.KottageItemEventRepository
import io.github.irgaly.kottage.internal.repository.KottageItemListRepository
import io.github.irgaly.kottage.internal.repository.KottageItemRepository
import io.github.irgaly.kottage.internal.repository.KottageStatsRepository

/**
 * Data Operation Logic of KottageStorage
 */
internal class KottageStorageOperator(
    private val storage: KottageStorage,
    private val operator: KottageOperator,
    private val itemRepository: KottageItemRepository,
    private val itemListRepository: KottageItemListRepository,
    private val itemEventRepository: KottageItemEventRepository,
    private val statsRepository: KottageStatsRepository
) {
    private val itemType = storage.name

    /**
     * This should be called in transaction
     */
    fun upsertItem(item: Item, now: Long) {
        val isCreate = !itemRepository.exists(item.key, item.type)
        itemRepository.upsert(item)
        if (isCreate) {
            itemRepository.incrementStatsCount(item.type, 1)
        }
        operator.addEvent(
            now = now,
            eventType = if (isCreate) ItemEventType.Create else ItemEventType.Update,
            eventExpireTime = storage.options.eventExpireTime,
            itemType = item.type,
            itemKey = item.key,
            itemListId = null,
            itemListType = null,
            maxEventEntryCount = storage.options.maxEventEntryCount
        )
        if (isCreate) {
            val count = itemRepository.getStatsCount(item.type)
            storage.options.strategy.onPostItemCreate(item.key, item.type, count, now, operator)
        }
    }

    /**
     * This should be called in transaction
     */
    fun getOrNull(key: String, now: Long?): Item? {
        var item = itemRepository.get(key, itemType)
        if ((now != null) && (item != null) && item.isExpired(now)) {
            val itemListEntryIds = itemListRepository.getIds(itemType = itemType, itemKey = key)
            if (itemListEntryIds.isEmpty()) {
                // ItemList に存在しなければ削除可能
                item = null
                operator.deleteItemInternal(key = key, itemType = itemType)
            }
        }
        return item
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
        now: Long,
        onEventCreated: (eventId: String) -> Unit
    ) {
        itemListRepository.getIds(
            itemType = itemType,
            itemKey = key
        ).forEach { itemListEntryId ->
            val entry = checkNotNull(itemListRepository.get(itemListEntryId))
            // ItemList から削除
            removeListItemInternal(
                positionId = entry.id,
                listType = entry.type
            )
            val eventId = operator.addEvent(
                now = now,
                eventType = ItemEventType.Delete,
                eventExpireTime = storage.options.eventExpireTime,
                itemType = itemType,
                itemKey = key,
                itemListId = entry.id,
                itemListType = entry.type,
                maxEventEntryCount = storage.options.maxEventEntryCount
            )
            onEventCreated(eventId)
        }
        operator.deleteItemInternal(key = key, itemType = itemType)
        val eventId = operator.addEvent(
            now = now,
            eventType = ItemEventType.Delete,
            eventExpireTime = storage.options.eventExpireTime,
            itemType = itemType,
            itemKey = key,
            itemListId = null,
            itemListType = null,
            maxEventEntryCount = storage.options.maxEventEntryCount
        )
        onEventCreated(eventId)
    }

    /**
     * This should be called in transaction
     */
    fun removeListItemInternal(positionId: String, listType: String) {
        itemListRepository.removeItemKey(id = positionId)
        itemListRepository.decrementStatsCount(listType, 1)
    }
}
