package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KottageStorageOptions
import io.github.irgaly.kottage.internal.database.Transaction
import io.github.irgaly.kottage.internal.model.Item
import io.github.irgaly.kottage.internal.model.ItemEventType
import io.github.irgaly.kottage.internal.model.ItemListEntry
import io.github.irgaly.kottage.internal.repository.KottageItemEventRepository
import io.github.irgaly.kottage.internal.repository.KottageItemListRepository
import io.github.irgaly.kottage.internal.repository.KottageItemRepository
import io.github.irgaly.kottage.internal.repository.KottageStatsRepository
import io.github.irgaly.kottage.strategy.KottageTransaction

/**
 * Data Operation Logic of KottageStorage
 */
internal class KottageStorageOperator(
    private val itemType: String,
    private val storageOptions: KottageStorageOptions,
    private val operator: KottageOperator,
    private val itemRepository: KottageItemRepository,
    private val itemListRepository: KottageItemListRepository,
    private val itemEventRepository: KottageItemEventRepository,
    private val statsRepository: KottageStatsRepository
) {
    /**
     * This should be called in transaction
     */
    suspend fun upsertItem(transaction: Transaction, item: Item, now: Long) {
        val isCreate = !itemRepository.exists(transaction, item.key, item.type)
        itemRepository.upsert(transaction, item)
        if (isCreate) {
            itemRepository.incrementStatsCount(transaction, item.type, 1)
        }
        operator.addEvent(
            transaction,
            now = now,
            eventType = if (isCreate) ItemEventType.Create else ItemEventType.Update,
            eventExpireTime = storageOptions.eventExpireTime,
            itemType = item.type,
            itemKey = item.key,
            itemListId = null,
            itemListType = null,
            maxEventEntryCount = storageOptions.maxEventEntryCount
        )
        if (isCreate) {
            val count = itemRepository.getStatsCount(transaction, item.type)
            storageOptions.strategy.onPostItemCreate(KottageTransaction(transaction), item.key, item.type, count, now, operator)
        }
    }

    /**
     * This should be called in transaction
     */
    suspend fun getOrNull(transaction: Transaction, key: String, now: Long?): Item? {
        var item = itemRepository.get(transaction, key, itemType)
        if ((now != null) && (item != null) && item.isExpired(now)) {
            val itemListEntryIds = itemListRepository.getIds(transaction, itemType = itemType, itemKey = key)
            if (itemListEntryIds.isEmpty()) {
                // ItemList に存在しなければ削除可能
                item = null
                operator.deleteItemInternal(transaction, key = key, itemType = itemType)
            }
        }
        return item
    }

    /**
     * This should be called in transaction
     */
    suspend fun getAllKeys(transaction: Transaction, receiver: suspend (key: String) -> Unit) {
        itemRepository.getAllKeys(transaction, itemType = itemType, receiver = receiver)
    }

    /**
     * Delete Item
     * This should be called in transaction
     *
     * * ItemList からも削除される
     * * ItemList / Item の Delete Event が登録される
     */
    suspend fun deleteItem(
        transaction: Transaction,
        key: String,
        now: Long,
        onEventCreated: (eventId: String) -> Unit
    ) {
        itemListRepository.getIds(
            transaction,
            itemType = itemType,
            itemKey = key
        ).forEach { itemListEntryId ->
            val entry = checkNotNull(itemListRepository.get(transaction, itemListEntryId))
            // ItemList から削除
            removeListItemInternal(
                transaction,
                positionId = entry.id,
                listType = entry.type,
                now = now,
                entry = entry
            )
            val eventId = operator.addEvent(
                transaction,
                now = now,
                eventType = ItemEventType.Delete,
                eventExpireTime = storageOptions.eventExpireTime,
                itemType = itemType,
                itemKey = key,
                itemListId = entry.id,
                itemListType = entry.type,
                maxEventEntryCount = storageOptions.maxEventEntryCount
            )
            onEventCreated(eventId)
        }
        operator.deleteItemInternal(transaction, key = key, itemType = itemType)
        val eventId = operator.addEvent(
            transaction,
            now = now,
            eventType = ItemEventType.Delete,
            eventExpireTime = storageOptions.eventExpireTime,
            itemType = itemType,
            itemKey = key,
            itemListId = null,
            itemListType = null,
            maxEventEntryCount = storageOptions.maxEventEntryCount
        )
        onEventCreated(eventId)
    }

    /**
     * This should be called in transaction
     */
    suspend fun exists(transaction: Transaction, key: String): Boolean {
        return itemRepository.exists(transaction, key = key, itemType = itemType)
    }

    /**
     * This should be called in transaction
     */
    suspend fun clear(transaction: Transaction, now: Long) {
        itemRepository.getAllKeys(transaction, itemType) { key ->
            itemListRepository.getIds(
                transaction,
                itemType = itemType,
                itemKey = key
            ).forEach { itemListEntryId ->
                val entry = checkNotNull(itemListRepository.get(transaction, itemListEntryId))
                removeListItemInternal(
                    transaction,
                    positionId = entry.id,
                    listType = entry.type,
                    now = now,
                    entry = entry
                )
            }
        }
        itemRepository.deleteAll(transaction, itemType)
        itemEventRepository.deleteAll(transaction, itemType)
        itemRepository.deleteStats(transaction, itemType)
    }

    /**
     * This should be called in transaction
     */
    suspend fun removeListItemInternal(
        transaction: Transaction,
        positionId: String,
        listType: String,
        now: Long,
        entry: ItemListEntry? = null
    ) {
        val current = entry
            ?: itemListRepository.get(transaction, positionId)
            ?: throw IllegalStateException("no entry: id = $positionId")
        if (listType != current.type) {
            throw IllegalStateException("invalid list type: listType = $listType, entry.type = ${current.type}")
        }
        if (current.expireAt?.let { now < it } != false) {
            // 削除時に expireAt を設定する
            // expireAt が未来の時刻なら上書きする
            itemListRepository.updateExpireAt(transaction, id = positionId, expireAt = now)
        }
        itemListRepository.removeItemKey(transaction, id = positionId)
        itemListRepository.removeUserData(transaction, id = positionId)
        itemListRepository.decrementStatsCount(transaction, listType, 1)
    }

    /**
     * This should be called in transaction
     */
    suspend fun getDebugStatus(transaction: Transaction): String {
        val stats = itemRepository.getStats(transaction, itemType)
        val count = itemRepository.getCount(transaction, itemType)
        return """
        [ storage "$itemType" ]
        Stats count: ${stats?.count ?: "(no stats)"}
        Stats event count: ${stats?.eventCount ?: "(no stats)"}
        SQL item count: $count
        """.trimIndent()
    }
}
