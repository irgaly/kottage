package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KottageList
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
     * 前後のアイテムと item_list_stats の先頭末尾情報を更新しながらアイテムを取り除く
     * 有効なアイテムを削除した場合は Delete Event を追加する
     */
    fun removeListEntries(positionIds: List<String>) {
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
            var deleted = 0
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
                        count = deleted.toLong()
                    )
                }
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
     * * リストの先頭から、expired な entity を削除する
     *     * 非削除対象の entity が現れたら処理を止める
     * * リストの末尾から、expired な entity を削除する
     *     * 非削除対象の entity が現れたら処理を止める
     * * リスト全体から、無効な entity を取り除く
     * * リストが空になれば item_list_stats を削除する
     */
    fun evictExpiredEntries(now: Long) {
        TODO("implement")
    }

    /**
     * This should be called in transaction
     */
    fun clear() {
        itemListRepository.deleteAll(type = listType)
        itemListRepository.deleteStats(type = listType)
        itemEventRepository.deleteAllList(listType = listType)
    }
}
