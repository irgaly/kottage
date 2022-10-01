package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.model.ItemListEntry
import io.github.irgaly.kottage.internal.model.ItemListStats

internal interface KottageItemListRepository {
    fun upsert(entry: ItemListEntry)
    fun updatePreviousId(id: String, previousId: String?)
    fun updateNextId(id: String, nextId: String?)
    fun updateItemKey(id: String, itemType: String, itemKey: String?, expireAt: Long?)
    fun removeItemKey(id: String)
    fun get(id: String): ItemListEntry?
    fun getIds(itemType: String, itemKey: String): List<String>
    fun getCount(type: String): Long
    fun delete(id: String)
    fun deleteAll(type: String)
    fun createStatsIfNotExists(
        type: String,
        firstItemListEntryId: String,
        lastItemListEntryId: String
    )

    fun getStats(type: String): ItemListStats?
    fun getStatsCount(type: String): Long
    fun incrementStatsCount(type: String, count: Long)
    fun decrementStatsCount(type: String, count: Long)
    fun updateStatsCount(type: String, count: Long)
    fun deleteStats(type: String)
}
