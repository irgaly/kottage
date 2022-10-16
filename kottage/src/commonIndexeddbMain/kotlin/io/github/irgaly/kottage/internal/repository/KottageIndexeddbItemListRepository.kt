package io.github.irgaly.kottage.internal.repository

import io.github.irgaly.kottage.internal.model.ItemListEntry
import io.github.irgaly.kottage.internal.model.ItemListStats

internal class KottageIndexeddbItemListRepository : KottageItemListRepository {
    override fun upsert(entry: ItemListEntry) {
        TODO("Not yet implemented")
    }

    override fun updatePreviousId(id: String, previousId: String?) {
        TODO("Not yet implemented")
    }

    override fun updateNextId(id: String, nextId: String?) {
        TODO("Not yet implemented")
    }

    override fun updateItemKey(id: String, itemType: String, itemKey: String?, expireAt: Long?) {
        TODO("Not yet implemented")
    }

    override fun updateExpireAt(id: String, expireAt: Long?) {
        TODO("Not yet implemented")
    }

    override fun removeItemKey(id: String) {
        TODO("Not yet implemented")
    }

    override fun removeUserData(id: String) {
        TODO("Not yet implemented")
    }

    override fun get(id: String): ItemListEntry? {
        TODO("Not yet implemented")
    }

    override fun getIds(itemType: String, itemKey: String): List<String> {
        TODO("Not yet implemented")
    }

    override fun getInvalidatedItemIds(
        type: String,
        beforeExpireAt: Long?,
        limit: Long
    ): List<String> {
        TODO("Not yet implemented")
    }

    override fun getCount(type: String): Long {
        TODO("Not yet implemented")
    }

    override fun getInvalidatedItemCount(type: String): Long {
        TODO("Not yet implemented")
    }

    override fun getAllTypes(receiver: (type: String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun delete(id: String) {
        TODO("Not yet implemented")
    }

    override fun deleteAll(type: String) {
        TODO("Not yet implemented")
    }

    override fun createStats(
        type: String,
        count: Long,
        firstItemListEntryId: String,
        lastItemListEntryId: String
    ) {
        TODO("Not yet implemented")
    }

    override fun getStats(type: String): ItemListStats? {
        TODO("Not yet implemented")
    }

    override fun getStatsCount(type: String): Long {
        TODO("Not yet implemented")
    }

    override fun incrementStatsCount(type: String, count: Long) {
        TODO("Not yet implemented")
    }

    override fun decrementStatsCount(type: String, count: Long) {
        TODO("Not yet implemented")
    }

    override fun updateStatsCount(type: String, count: Long) {
        TODO("Not yet implemented")
    }

    override fun updateStatsFirstItem(type: String, id: String) {
        TODO("Not yet implemented")
    }

    override fun updateStatsLastItem(type: String, id: String) {
        TODO("Not yet implemented")
    }

    override fun deleteStats(type: String) {
        TODO("Not yet implemented")
    }
}
